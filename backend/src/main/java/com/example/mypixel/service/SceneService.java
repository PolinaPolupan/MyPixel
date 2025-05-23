package com.example.mypixel.service;

import com.example.mypixel.exception.SceneNotFoundException;
import com.example.mypixel.model.Scene;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SceneService {

    private final StorageService storageService;
    private final SceneRepository sceneRepository;

    public Scene createScene() {
        Scene scene = Scene
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .version(1L)
                .build();

        scene = sceneRepository.save(scene);

        String sceneId = scene.getId().toString();

        storageService.createFolder("scenes/" + sceneId + "/output");
        storageService.createFolder("scenes/" + sceneId + "/input");

        return scene;
    }

    @Transactional
    public void updateLastAccessed(Long sceneId) {
        if (!sceneRepository.existsById(sceneId)) {
            throw new SceneNotFoundException("Scene with id: " + sceneId + " not found");
        }

        sceneRepository.updateLastAccessedTime(sceneId, LocalDateTime.now());
    }

    List<Scene> getInactiveScenes() {
        return sceneRepository.findSceneIdsByStatusNotInAndLastAccessedBefore(
                List.of(TaskStatus.PENDING, TaskStatus.RUNNING), LocalDateTime.now().minusHours(1));
    }

    public void deleteScene(Long sceneId) {
        storageService.delete("scenes/" + sceneId.toString());
        sceneRepository.deleteById(sceneId);
    }
}
