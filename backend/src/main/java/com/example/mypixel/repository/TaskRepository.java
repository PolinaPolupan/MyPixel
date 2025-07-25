package com.example.mypixel.repository;

import com.example.mypixel.model.Task;
import com.example.mypixel.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatusNotIn(List<TaskStatus> statuses);
}