package com.example.mypixel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


@ExtendWith(MockitoExtension.class)
public class FileHelperTests {

    @Mock
    private StorageService storageService;

    private static final Long SCENE_ID = 123L;
    private static final Long TASK_ID = 123L;
    private static final Long NODE_ID = 456L;

    @BeforeEach
    void setUp() {
       FileHelper.setStorageService(storageService);
    }

    @Nested
    @DisplayName("Extract Filename Tests")
    class ExtractFilenameTests {

        @ParameterizedTest
        @CsvSource({
                "/path/to/file.jpg, file.jpg",
                "file.jpg, file.jpg",
                "/path/with/multiple/slashes/file.jpg, file.jpg",
                "path/to/filename.with.dots.jpg, filename.with.dots.jpg"
        })
        void shouldExtractFilenameCorrectly(String input, String expected) {
            assertEquals(expected, FileHelper.extractFilename(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldHandleNullAndEmptyInput(String input) {
            assertEquals("", FileHelper.extractFilename(input));
        }

        @Test
        void shouldHandlePathEndingWithSlash() {
            assertEquals("", FileHelper.extractFilename("/path/to/directory/"));
        }
    }

    @Nested
    @DisplayName("Extract Path Tests")
    class ExtractPathTests {

        @Test
        void shouldExtractPath() {
            String filepath = "123/input/pic/Picture.jpeg";
            assertEquals("123/input/pic/", FileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractEmptyPath() {
            String filepath = "Picture.jpeg";
            assertEquals("", FileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractPathAfterInput() {
            String filepath = "scenes/" + SCENE_ID + "/input/pic/Picture.jpeg";
            assertEquals("pic/", FileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldExtractPathAfterId() {
            String filepath = "tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture.jpeg";
            assertEquals("output/", FileHelper.extractRelativeWorkspacePath(filepath));
        }


        @Test
        void shouldHandleMultipleSubfoldersAfterInput() {
            String filepath = "scenes/" + SCENE_ID + "/input/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", FileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleMultipleSubfoldersAfter() {
            String filepath = "tasks/" + TASK_ID + "/" + NODE_ID + "/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", FileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleInputAsLastSegment() {
            String filepath = "scenes" + SCENE_ID + "/input";
            assertEquals("", FileHelper.extractRelativeWorkspacePath(filepath));
        }
    }

    @Nested
    @DisplayName("Add Prefix To Filename Tests")
    class AddPrefixToFilenameTests {

        @ParameterizedTest
        @CsvSource({
                "file.jpg, prefix, prefix_file.jpg",
                "/path/to/file.jpg, prefix, prefix_file.jpg",
                "file.with.dots.jpg, pre, pre_file.with.dots.jpg",
                "file, prefix, prefix_file"
        })
        void shouldAddPrefixCorrectly(String filename, String prefix, String expected) {
            assertEquals(expected, FileHelper.addPrefixToFilename(filename, prefix));
        }
    }

    @Nested
    @DisplayName("Store To Output Tests")
    class StoreToOutputTests {

        @Test
        void shouldStoreFileToOutputWithoutFolderOrPrefix() {
            String filepath = "scenes/" + SCENE_ID + "/input/picture.jpg";
            String outputPath = "scenes/" + SCENE_ID + "/picture.jpg";

            String result = FileHelper.storeFromWorkspaceToScene(SCENE_ID, filepath, null, null);

            verify(storageService).store(filepath, outputPath);
            assertEquals(outputPath, result);
        }

        @Test
        void shouldStoreFileToOutputWithFolderAndPrefix() {
            String filepath = "scenes/" + SCENE_ID + "/input/picture.jpg";
            String folder = "processed";
            String prefix = "edited";
            String outputPath = "scenes/" + SCENE_ID + "/processed/edited_picture.jpg";

            String result = FileHelper.storeFromWorkspaceToScene(SCENE_ID, filepath, folder, prefix);

            verify(storageService).store(filepath, outputPath);
            assertEquals(outputPath, result);
        }
    }

    @Nested
    @DisplayName("Store To Temp Tests")
    class StoreToTempTests {

        @Test
        void shouldStoreFileToTemp() {
            String filename = "path/temp-file.jpg";
            String tempPath = "tasks/" + TASK_ID + "/" + NODE_ID + "/" + filename;
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

            String result = FileHelper.storeToTask(TASK_ID, NODE_ID, inputStream, filename);

            verify(storageService).store(inputStream, tempPath);
            assertEquals(tempPath, result);
        }
    }

    @Nested
    @DisplayName("Create Dump Tests")
    class CreateDumpTests {

        @Test
        void shouldCreateDumpFile() {
            String filepath = SCENE_ID + "/input/picture.jpg";
            String dumpPath = "tasks/" + TASK_ID + "/" + NODE_ID + "/picture.jpg";

            String result = FileHelper.storeFromWorkspaceToTask(TASK_ID, NODE_ID, filepath);

            verify(storageService).store(filepath, dumpPath);
            assertEquals(dumpPath, result);
        }

        @Test
        void shouldCreateDumpFromTempFile() {
            String filepath = "tasks/" + "78" + "/" + "768" + "/output/Picture1.png";
            String dumpPath = "tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png";

            String result = FileHelper.storeFromWorkspaceToTask(TASK_ID, NODE_ID, filepath);

            verify(storageService).store(filepath, dumpPath);
            assertEquals(dumpPath, result);
        }

        @Test
        void shouldThrowExceptionWhenResourceIsNull() {
            String filepath = "/root/path/tasks/" + TASK_ID + "/" + NODE_ID + "/input/picture.jpg";

            doThrow(RuntimeException.class).when(storageService).store(anyString(), anyString());

            assertThrows(RuntimeException.class, () -> FileHelper.storeFromWorkspaceToTask(TASK_ID, NODE_ID, filepath));
        }
    }
}