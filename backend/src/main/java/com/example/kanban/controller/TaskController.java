package com.example.kanban.controller;

import com.example.kanban.dto.MoveTaskRequest;
import com.example.kanban.dto.TaskDTO;
import com.example.kanban.entity.Task.TaskStatus;
import com.example.kanban.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    @GetMapping
    // 모든 업무 조회
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
    
    @GetMapping("/status/{status}")
    // 상태별 업무 조회
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }
    
    // 업무 생성
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskDTO));
    }
    
    // 업무 수정
    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO));
    }
    
    // 업무 이동 (드래그앤드롭)
    @PatchMapping("/{id}/move")
    public ResponseEntity<Void> moveTask(
            @PathVariable Long id,
            @Valid @RequestBody MoveTaskRequest request){
    taskService.moveTask(id, request.getStatus(), request.getPosition());
    return ResponseEntity.ok().build();
    }
    
    // 업무 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}