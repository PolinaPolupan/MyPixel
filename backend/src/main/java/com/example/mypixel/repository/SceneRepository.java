package com.example.mypixel.repository;

import com.example.mypixel.model.Scene;
import com.example.mypixel.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    @Query("""
        SELECT s
        FROM Task t
        JOIN Scene s ON s.id = t.sceneId
        WHERE t.status NOT IN (:statuses)
          AND s.lastAccessed <= :lastAccessBefore
    """)
    List<Scene> findSceneIdsByStatusNotInAndLastAccessedBefore(
            @Param("statuses") List<TaskStatus> statuses,
            @Param("lastAccessBefore") LocalDateTime lastAccessBefore
    );

    @Modifying
    @Query("UPDATE Scene s SET s.lastAccessed = :now WHERE s.id = :sceneId")
    void updateLastAccessedTime(@Param("sceneId") Long sceneId, @Param("now") LocalDateTime now);
}
