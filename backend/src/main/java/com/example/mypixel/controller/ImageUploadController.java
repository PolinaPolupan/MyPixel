package com.example.mypixel.controller;


import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.mypixel.exception.InvalidImageFormat;
import com.example.mypixel.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import software.amazon.awssdk.annotations.NotNull;

@RestController
@RequestMapping("/v1/scene/{sceneId}")
public class ImageUploadController {

    private final StorageService storageService;

    @Autowired
    public ImageUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/input")
    public List<String> listUploadedFiles(@PathVariable String sceneId) {
        return storageService.loadAll(sceneId + "/input/").map(
                        path -> MvcUriComponentsBuilder.fromMethodName(ImageUploadController.class,
                                "serveFile", sceneId, path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/output")
    public List<String> listOutputFiles(@PathVariable String sceneId) {
        return storageService.loadAll(sceneId + "/output/").map(
                        path -> MvcUriComponentsBuilder.fromMethodName(ImageUploadController.class,
                                "serveOutputFile", sceneId, path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/input/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String sceneId, @PathVariable String filename) {
        Resource file = storageService.loadAsResource(sceneId + "/input/" + filename);

        return getResourceResponseEntity(file);
    }

    @GetMapping("/output/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveOutputFile(@PathVariable String sceneId, @PathVariable String filename) {
        Resource file = storageService.loadAsResource(sceneId + "/output/" + filename);

        return getResourceResponseEntity(file);
    }

    @NotNull
    private ResponseEntity<Resource> getResourceResponseEntity(Resource file) {
        if (file == null)
            return ResponseEntity.notFound().build();

        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(file.getFile().getAbsolutePath()));
        } catch (IOException e) {
            contentType = URLConnection.guessContentTypeFromName(file.getFilename());
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/input")
    public ResponseEntity<List<Map<String, String>>> handleFileUpload(@PathVariable String sceneId,
                                                 @RequestParam("file") List<MultipartFile> files) {

        List<Map<String, String>> responses = new ArrayList<>();

        for (MultipartFile file: files) {
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new InvalidImageFormat("Only JPEG or PNG images are allowed");
            }

            storageService.store(file, sceneId + "/input/" + file.getOriginalFilename());

            String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/scenes/")
                    .path(sceneId)
                    .path("/input/")
                    .path(file.getOriginalFilename())
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("fileLocation", fileUri);
            responses.add(response);
        }

        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }
}
