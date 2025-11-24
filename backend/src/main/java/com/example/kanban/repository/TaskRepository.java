package com.example.kanban.repository;

import com.example.kanban.entity.Task;
import com.example.kanban.entity.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByStatusOrderByPositionAsc(TaskStatus status);
    
    List<Task> findAllByOrderByStatusAscPositionAsc();
}