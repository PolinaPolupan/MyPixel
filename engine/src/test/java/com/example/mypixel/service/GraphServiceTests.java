package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Task;
import com.example.mypixel.model.TaskPayload;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.model.Node;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GraphServiceTests {

    @Mock
    private NodeProcessorService nodeProcessorService;

    @Mock
    private PerformanceTracker performanceTracker;

    @Mock
    private TaskService taskService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GraphService graphService;

    private Graph graph;

    private Task task;

    private Node node1, node2;

    private final Long sceneId = 1L;
    private final Long taskId = 100L;

    @BeforeEach
    void setup() {
        node1 = mock(Node.class);
        node2 = mock(Node.class);

        List<Node> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);

        graph = mock(Graph.class);
        Iterator<Node> mockIterator = nodes.iterator();
        when(graph.iterator()).thenReturn(mockIterator);

        task = new Task();
        task.setId(taskId);
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.PENDING);
        task.setTotalNodes(2);
        task.setProcessedNodes(0);

        when(performanceTracker.trackOperation(anyString(), any(Tags.class), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(2);
                    return supplier.get();
                });

        when(taskService.createTask(any(Graph.class), anyLong()))
                .thenReturn(task);

        when(taskService.findTaskById(taskId)).thenReturn(task);
    }

    @Test
    void startGraphExecution_shouldCreateTaskAndReturnCompletedTask() throws ExecutionException, InterruptedException {
        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);
        TaskPayload result = future.get();

        verify(taskService).createTask(graph, sceneId);
        verify(taskService).updateTaskStatus(taskId, TaskStatus.RUNNING);
        verify(taskService).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(notificationService, times(3)).sendTaskStatus(any(TaskPayload.class));
        assertEquals(TaskPayload.fromEntity(task), result);
    }

    @Test
    void startGraphExecutionAsync_shouldCreateTaskAndReturnFuture() throws Exception {
        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);
        TaskPayload result = future.get();

        verify(taskService).createTask(graph, sceneId);
        verify(taskService).updateTaskStatus(taskId, TaskStatus.RUNNING);
        verify(taskService).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(notificationService, times(3)).sendTaskStatus(any(TaskPayload.class));
        assertEquals(TaskPayload.fromEntity(task), result);
    }

    @Test
    void executeGraphInternal_shouldProcessAllNodesInOrder() throws Exception {
        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);
        future.get();

        InOrder inOrder = inOrder(nodeProcessorService, taskService, notificationService);

        inOrder.verify(taskService).updateTaskStatus(taskId, TaskStatus.RUNNING);

        inOrder.verify(nodeProcessorService).processNode(node1, sceneId, taskId);
        inOrder.verify(taskService).updateTaskProgress(taskId, 1);
        inOrder.verify(notificationService).sendTaskStatus(any(TaskPayload.class));

        inOrder.verify(nodeProcessorService).processNode(node2, sceneId, taskId);
        inOrder.verify(taskService).updateTaskProgress(taskId, 2);
        inOrder.verify(notificationService).sendTaskStatus(any(TaskPayload.class));

        inOrder.verify(taskService).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        inOrder.verify(notificationService).sendTaskStatus(any(TaskPayload.class));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraphInternal_whenEmptyGraph_shouldCompleteSuccessfully() throws Exception {
        Graph emptyGraph = mock(Graph.class);
        List<Node> emptyList = new ArrayList<>();
        when(emptyGraph.getNodes()).thenReturn(emptyList);
        when(emptyGraph.iterator()).thenReturn(emptyList.iterator());

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(emptyGraph, sceneId);
        TaskPayload result = future.get();

        verify(taskService).updateTaskStatus(taskId, TaskStatus.RUNNING);
        verify(taskService, never()).updateTaskProgress(any(), anyInt());
        verify(taskService).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(notificationService).sendTaskStatus(any(TaskPayload.class));
        assertEquals(TaskPayload.fromEntity(task), result);
    }

    @Test
    void executeGraphInternal_whenNodeProcessingFails_shouldMarkTaskFailedAndCompleteFutureExceptionally() {
        String errorMessage = "Node processing failed";
        doThrow(new RuntimeException(errorMessage))
                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals(errorMessage, exception.getCause().getMessage());

        verify(taskService).updateTaskStatus(taskId, TaskStatus.RUNNING);
        verify(taskService).markTaskFailed(taskId, errorMessage);
        verify(notificationService, atLeastOnce()).sendTaskStatus(any(TaskPayload.class));
        verify(taskService, never()).updateTaskStatus(taskId, TaskStatus.COMPLETED);
    }

    @Test
    void executeGraphInternal_shouldUpdateProgressCorrectly() throws Exception {
        List<Node> manyNodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            manyNodes.add(mock(Node.class));
        }

        when(graph.iterator()).thenReturn(manyNodes.iterator());

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);
        future.get();

        ArgumentCaptor<Integer> progressCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(taskService, times(5)).updateTaskProgress(eq(taskId), progressCaptor.capture());

        List<Integer> progressUpdates = progressCaptor.getAllValues();
        assertEquals(5, progressUpdates.size());

        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, progressUpdates.get(i).intValue());
        }

        ArgumentCaptor<TaskPayload> notificationProgressCaptor = ArgumentCaptor.forClass(TaskPayload.class);
        verify(notificationService, times(6)).sendTaskStatus(notificationProgressCaptor.capture());

        List<TaskPayload> notificationProgress = notificationProgressCaptor.getAllValues();
        assertEquals(6, notificationProgress.size());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void startGraphExecution_shouldHandleExceptionDuringExecution() {
        String errorMessage = "Task creation failed";
        when(taskService.createTask(any(), anyLong()))
                .thenThrow(new RuntimeException(errorMessage));

        Exception exception = assertThrows(RuntimeException.class, () ->
                graphService.startGraphExecution(graph, sceneId));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void startGraphExecutionAsync_shouldHandleExceptionInTaskCreation() {
        String errorMessage = "Task creation failed";
        when(taskService.createTask(any(), anyLong()))
                .thenThrow(new RuntimeException(errorMessage));

        Exception exception = assertThrows(RuntimeException.class, () ->
                graphService.startGraphExecutionAsync(graph, sceneId));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraph_shouldUsePerformanceTracker() {
        reset(performanceTracker);
        CompletableFuture<TaskPayload> expectedFuture = CompletableFuture.completedFuture(TaskPayload.fromEntity(task));

        when(performanceTracker.trackOperation(anyString(), any(Tags.class), any(Supplier.class)))
                .thenReturn(expectedFuture);

        CompletableFuture<TaskPayload> actualFuture = graphService.executeGraph(graph, taskId, sceneId);

        verify(performanceTracker).trackOperation(
                eq("graph.execution"),
                eq(Tags.of("scene.id", String.valueOf(sceneId))),
                any(Supplier.class)
        );

        assertSame(expectedFuture, actualFuture);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraphInternal_shouldHandleInterruptedException() {
        doThrow(new RuntimeException("Thread interrupted"))
                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());

        verify(taskService).markTaskFailed(eq(taskId), contains("Thread interrupted"));
        verify(notificationService, atLeastOnce()).sendTaskStatus(any(TaskPayload.class));
    }

    @Test
    void executeGraphInternal_shouldLogErrorDetails() throws Exception {
        String errorMessage = "Test error message";
        RuntimeException testException = new RuntimeException(errorMessage);
        doThrow(testException)
                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);

        try {
            future.get();
        } catch (ExecutionException expected) {
        }

        verify(taskService).markTaskFailed(eq(taskId), eq(errorMessage));
        verify(notificationService, atLeastOnce()).sendTaskStatus(any(TaskPayload.class));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraphInternal_whenTaskStatusUpdateFails_shouldHandleGracefully() {
        doThrow(new RuntimeException("Status update failed"))
                .when(taskService).updateTaskStatus(any(), eq(TaskStatus.RUNNING));

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Status update failed", exception.getCause().getMessage());

        verify(taskService).markTaskFailed(eq(taskId), contains("Status update failed"));
        verify(notificationService, atLeastOnce()).sendTaskStatus(any(TaskPayload.class));

        verify(nodeProcessorService, never()).processNode(any(), anyLong(), anyLong());
    }

    @Test
    void executeGraphInternal_whenNotificationFails_shouldContinueExecution() throws Exception {
        doAnswer(invocation -> {
            log.error("Failed to send progress WebSocket: Notification failed");
            return null;
        }).when(notificationService).sendTaskStatus(any(TaskPayload.class));

        CompletableFuture<TaskPayload> future = graphService.startGraphExecutionSync(graph, sceneId);
        TaskPayload result = future.get();

        verify(taskService).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(nodeProcessorService, times(2)).processNode(any(), anyLong(), anyLong());
        assertEquals(TaskPayload.fromEntity(task), result);
    }
}