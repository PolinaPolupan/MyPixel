package com.example.mypixel.service;

import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<GraphExecutionTask> taskCaptor;

    private final Long sceneId = 1L;
    private final Long taskId = 1L;
    private GraphExecutionTask task;

    @BeforeEach
    void setUp() {
        task = new GraphExecutionTask();
        task.setId(taskId);
        task.setSceneId(sceneId);
    }

    @Test
    void sendProgress_shouldSendCorrectMessage() {
        int processed = 5;
        int total = 10;

        task.setStatus(TaskStatus.RUNNING);
        task.setProcessedNodes(processed);
        task.setTotalNodes(total);

        notificationService.sendTaskStatus(task);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        GraphExecutionTask sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getSceneId());
        assertEquals(TaskStatus.RUNNING, sentTask.getStatus());
        assertEquals(processed, sentTask.getProcessedNodes());
        assertEquals(total, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_withZeroTotal_shouldHandleZeroDivision() {
        task.setStatus(TaskStatus.RUNNING);
        task.setProcessedNodes(0);
        task.setTotalNodes(0);

        notificationService.sendTaskStatus(task);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        GraphExecutionTask sentTask = taskCaptor.getValue();
        assertEquals(0, sentTask.getProcessedNodes());
        assertEquals(0, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        task.setStatus(TaskStatus.RUNNING);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(GraphExecutionTask.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(task));
    }

    @Test
    void sendCompleted_shouldSendCorrectMessage() {
        task.setStatus(TaskStatus.COMPLETED);

        notificationService.sendTaskStatus(task);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        GraphExecutionTask sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getSceneId());
        assertEquals(TaskStatus.COMPLETED, sentTask.getStatus());
    }

    @Test
    void sendCompleted_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        task.setStatus(TaskStatus.COMPLETED);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(GraphExecutionTask.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(task));
    }

    @Test
    void sendError_shouldSendCorrectMessage() {
        String errorMessage = "Test error message";
        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);

        notificationService.sendTaskStatus(task);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        GraphExecutionTask sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getSceneId());
        assertEquals(TaskStatus.FAILED, sentTask.getStatus());
        assertEquals(errorMessage, sentTask.getErrorMessage());
    }

    @Test
    void sendError_withNullErrorMessage_shouldHandleNullValue() {
        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(null);

        notificationService.sendTaskStatus(task);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        GraphExecutionTask sentTask = taskCaptor.getValue();
        assertNull(sentTask.getErrorMessage());
    }

    @Test
    void sendError_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage("Error");
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(GraphExecutionTask.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(task));
    }

    @Test
    void sendTaskStatus_withNullId_shouldUseNullInDestination() {
        task.setId(null);
        task.setStatus(TaskStatus.RUNNING);

        notificationService.sendTaskStatus(task);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), any(GraphExecutionTask.class));

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/processing/null", destination);
    }
}