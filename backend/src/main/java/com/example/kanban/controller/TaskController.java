package com.example.kanban.controller;

import com.example.kanban.dto.TaskDTO;
import com.example.kanban.entity.Task.TaskStatus;
import com.example.kanban.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class TaskController {
    
    private final TaskService taskService;
    
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }
    
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskDTO));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO));
    }
    
    @PatchMapping("/{id}/move")
    public ResponseEntity<Void> moveTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> moveData) {
        TaskStatus newStatus = TaskStatus.valueOf((String) moveData.get("status"));
        Integer newPosition = (Integer) moveData.get("position");
        taskService.moveTask(id, newStatus, newPosition);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}