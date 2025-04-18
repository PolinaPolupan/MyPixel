//package com.example.mypixel.service;
//
//
//import com.example.mypixel.exception.InvalidNodeParameter;
//import com.example.mypixel.model.NodeReference;
//import com.example.mypixel.model.node.*;
//import com.example.mypixel.NodeType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.Resource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//public class NodeProcessorServiceTests {
//
//    @MockitoBean
//    private FilteringService filteringService;
//
//    @Autowired
//    private NodeProcessorService nodeProcessorService;
//
//    @Autowired
//    private FileManager fileManager;
//
//    @Mock
//    private Resource resource;
//
//    private String sceneId;
//
//    @BeforeEach
//    public void setUp() {
//        sceneId = "test-scene-" + UUID.randomUUID();
//
//        fileManager.createScene(sceneId);
//    }
//
//    @Test
//    public void testProcessInputNode() {
//        Resource mockResource = mock(Resource.class);
//        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), Map.of("files", List.of("input.jpg")));
//
//        when(fileManager.loadAsResource("input.jpg", sceneId)).thenReturn(mockResource);
//        when(fileManager.loadAsResource("input.jpg", sceneId)).thenReturn(mockResource);
//        when(mockResource.getFilename()).thenReturn("input.jpg");
//
//        nodeProcessorService.processNode(inputNode);
//
//        verify(fileManager, times(1)).createDump("input.jpg", sceneId);
//    }
//
////    @Test
////    public void testProcessNullInputNode() {
////        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), new HashMap<>() {});
////
////        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(inputNode));
////    }
////
////    @Test
////    public void testProcessEmptyGaussianBlurNode() {
////        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
////                "files", List.of(),
////                "sizeX", 5,
////                "sizeY", 5,
////                "sigmaX", 5,
////                "sigmaY", 5));
////
////        nodeProcessorService.processNode(node);
////
////        verify(tempStorageService, never()).createTempFileFromResource(any());
////    }
////
////    @Test
////    public void testProcessGaussianBlurNode() {
////        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
////                "files", List.of("input.jpeg"),
////                "sizeX", 5,
////                "sizeY", 5,
////                "sigmaX", 5.0,
////                "sigmaY", 5.0));
////
////        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpeg");
////
////        nodeProcessorService.processNode(node);
////
////        verify(filteringService, times(1))
////                .gaussianBlur("input.jpeg", 5, 5, 5.0, 5.0);
////    }
////
////    @Test
////    public void testProcessGaussianBlurNodeWithNoParameters() {
////        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
////                "files", List.of("input.jpeg"), "sizeX", 5));
////
////        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpeg");
////
////        nodeProcessorService.processNode(node);
////
////        verify(filteringService, times(1))
////                .gaussianBlur("input.jpeg", 5, 5, 0.0, 0.0);
////    }
////
////    @Test
////    public void testProcessOutputNode() {
////        Node node = new OutputNode(0L, NodeType.OUTPUT.getName(), Map.of(
////                "files", List.of("input.jpeg"),
////                "prefix", "output"
////        ));
////
////        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource("input.jpeg"))).thenReturn("input.jpeg");
////        when(tempStorageService.removeExistingPrefix("input.jpeg")).thenReturn("input.jpeg");
////        nodeProcessorService.processNode(node);
////
////        verify(storageService, times(1)).store(eq(resource), eq("output_input.jpeg"));
////    }
////
////    @Test
////    public void testProcessOutputNodeWithoutPrefix() {
////        Node node = new OutputNode(0L, NodeType.OUTPUT.getName(), Map.of("files", List.of("input.jpeg")));
////
////        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource("input.jpeg"))).thenReturn("input.jpeg");
////        when(tempStorageService.removeExistingPrefix("input.jpeg")).thenReturn("input.jpeg");
////        nodeProcessorService.processNode(node);
////
////        verify(storageService, times(1)).store(eq(resource), eq("input.jpeg"));
////    }
////
////    @Test
////    public void testProcessNodeWithInvalidNodeReference() {
////        Map<String, Object> inputs = new HashMap<>();
////        inputs.put("files", new NodeReference("@node:999:files"));
////        inputs.put("sizeX", 5);
////        inputs.put("sizeY", 5);
////        inputs.put("sigmaX", 5.0);
////        inputs.put("sigmaY", 5.0);
////
////        Node node = new GaussianBlurNode(1L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
////
////        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
////    }
////
////    @Test
////    public void testProcessNodeWithInvalidOutputReference() {
////        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), Map.of("files", List.of("input.jpg")));
////        when(storageService.loadAsResource("input.jpg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpg");
////        nodeProcessorService.processNode(inputNode);
////
////        Map<String, Object> inputs = new HashMap<>();
////        inputs.put("files", new NodeReference("@node:0:nonexistentOutput"));
////        inputs.put("sizeX", 5);
////        inputs.put("sizeY", 5);
////        inputs.put("sigmaX", 5.0);
////        inputs.put("sigmaY", 5.0);
////
////        Node blurNode = new GaussianBlurNode(1L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
////
////        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(blurNode));
////    }
////
////    @Test
////    public void testProcessNodeWithInvalidInputType() {
////        Map<String, Object> inputs = new HashMap<>();
////        inputs.put("files", List.of("input.jpg"));
////        inputs.put("sizeX", "not-a-number");
////        inputs.put("sizeY", 5);
////        inputs.put("sigmaX", 5.0);
////        inputs.put("sigmaY", 5.0);
////
////        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
////        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpg");
////
////        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
////    }
////
////    @Test
////    public void testProcessNodeWithMissingRequiredInput() {
////        Map<String, Object> inputs = new HashMap<>();
////
////        inputs.put("sizeX", 5);
////        inputs.put("sizeY", 5);
////
////        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
////
////        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
////    }
////
////    @Test
////    public void testProcessNodeWithNullInputType() {
////        Map<String, Object> inputs = new HashMap<>();
////        inputs.put("files", null);
////        inputs.put("sizeX", 5);
////        inputs.put("sizeY", 5);
////        inputs.put("sigmaX", 5.0);
////        inputs.put("sigmaY", 5.0);
////
////        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
////        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(resource);
////        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpg");
////
////        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
////    }
////
////    @Test
////    public void testS3InputNodeValidation() {
////        // Test with all required fields
////        Map<String, Object> validInputs = Map.of(
////                "access_key_id", "AKIAIOSFODNN7EXAMPLE",
////                "secret_access_key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
////                "region", "us-west-2",
////                "bucket", "my-sample-bucket",
////                "key", "sample-file.jpg"
////        );
////
////        Node s3InputNode = new S3InputNode(0L, "S3Input", validInputs);
////        // Should not throw an exception
////        s3InputNode.validate();
////
////        // Test with blank access key ID
////        Map<String, Object> blankAccessKeyInputs = new HashMap<>(validInputs);
////        blankAccessKeyInputs.put("access_key_id", "");
////
////        Node s3InputNodeBlankAccessKey = new S3InputNode(1L, "S3Input", blankAccessKeyInputs);
////        assertThrows(
////                InvalidNodeParameter.class,
////                s3InputNodeBlankAccessKey::validate
////        );
////
////        // Test with blank secret access key
////        Map<String, Object> blankSecretInputs = new HashMap<>(validInputs);
////        blankSecretInputs.put("secret_access_key", "");
////
////        Node s3InputNodeBlankSecret = new S3InputNode(2L, "S3Input", blankSecretInputs);
////        assertThrows(
////                InvalidNodeParameter.class,
////                s3InputNodeBlankSecret::validate
////        );
////
////        // Test with blank region
////        Map<String, Object> blankRegionInputs = new HashMap<>(validInputs);
////        blankRegionInputs.put("region", "");
////
////        Node s3InputNodeBlankRegion = new S3InputNode(3L, "S3Input", blankRegionInputs);
////        assertThrows(
////                InvalidNodeParameter.class,
////                s3InputNodeBlankRegion::validate
////        );
////
////        // Test with blank bucket name
////        Map<String, Object> blankBucketInputs = new HashMap<>(validInputs);
////        blankBucketInputs.put("bucket", "");
////
////        Node s3InputNodeBlankBucket = new S3InputNode(4L, "S3Input", blankBucketInputs);
////        assertThrows(
////                InvalidNodeParameter.class,
////                s3InputNodeBlankBucket::validate
////        );
//  //  }
//}
