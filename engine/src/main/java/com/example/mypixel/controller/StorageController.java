package com.example.mypixel.controller;

import com.example.mypixel.service.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/storage")
@Slf4j
public class StorageController {

    @PostMapping("/workspace-to-scene")
    public ResponseEntity<Map<String, String>> storeFromWorkspaceToScene(
            @RequestParam("sceneId") Long sceneId,
            @RequestParam("source") String source,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "prefix", required = false) String prefix
    ) {
        try {
            String targetPath = FileHelper.storeFromWorkspaceToScene(sceneId, source, folder, prefix);

            Map<String, String> response = new HashMap<>();
            response.put("path", targetPath);
            response.put("message", "File stored successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error storing file from workspace to scene", e);

            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/to-task", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> storeToTask(
            @RequestParam("taskId") Long taskId,
            @RequestParam("nodeId") Long nodeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("target") String target
    ) {
        try {
            String targetPath = FileHelper.storeToTask(taskId, nodeId, file.getInputStream(), target);

            Map<String, String> response = new HashMap<>();
            response.put("path", targetPath);
            response.put("message", "File stored successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error storing file to task", e);

            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/workspace-to-task")
    public ResponseEntity<Map<String, String>> storeFromWorkspaceToTask(
            @RequestParam("taskId") Long taskId,
            @RequestParam("nodeId") Long nodeId,
            @RequestParam("source") String source
    ) {
        try {
            String targetPath = FileHelper.storeFromWorkspaceToTask(taskId, nodeId, source);

            Map<String, String> response = new HashMap<>();
            response.put("path", targetPath);
            response.put("message", "File stored successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error storing file from workspace to task", e);

            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}