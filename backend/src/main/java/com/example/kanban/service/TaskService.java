package com.example.kanban.service;

import com.example.kanban.dto.TaskDTO;
import com.example.kanban.entity.Task;
import com.example.kanban.entity.Task.TaskStatus;
import com.example.kanban.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAllByOrderByStatusAscPositionAsc()
                .stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByPositionAsc(status)
                .stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public TaskDTO createTask(TaskDTO taskDTO) {
        List<Task> tasksInSameStatus = taskRepository.findByStatusOrderByPositionAsc(taskDTO.getStatus());
        int newPosition = tasksInSameStatus.isEmpty() ? 0 : tasksInSameStatus.size();
        
        taskDTO.setPosition(newPosition);
        Task task = taskRepository.save(taskDTO.toEntity());
        return TaskDTO.fromEntity(task);
    }
    
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: " + id));
        
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setAssignee(taskDTO.getAssignee());
        task.setPriority(taskDTO.getPriority());
        
        return TaskDTO.fromEntity(taskRepository.save(task));
    }
    
    public void moveTask(Long taskId, TaskStatus newStatus, Integer newPosition) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: " + taskId));
        
        TaskStatus oldStatus = task.getStatus();
        Integer oldPosition = task.getPosition();
        
        if (!oldStatus.equals(newStatus)) {
            List<Task> oldStatusTasks = taskRepository.findByStatusOrderByPositionAsc(oldStatus);
            for (Task t : oldStatusTasks) {
                if (t.getPosition() > oldPosition) {
                    t.setPosition(t.getPosition() - 1);
                    taskRepository.save(t);
                }
            }
        }
        
        List<Task> newStatusTasks = taskRepository.findByStatusOrderByPositionAsc(newStatus);
        for (Task t : newStatusTasks) {
            if (!t.getId().equals(taskId) && t.getPosition() >= newPosition) {
                t.setPosition(t.getPosition() + 1);
                taskRepository.save(t);
            }
        }
        
        task.setStatus(newStatus);
        task.setPosition(newPosition);
        taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: " + id));
        
        List<Task> tasksInSameStatus = taskRepository.findByStatusOrderByPositionAsc(task.getStatus());
        for (Task t : tasksInSameStatus) {
            if (t.getPosition() > task.getPosition()) {
                t.setPosition(t.getPosition() - 1);
                taskRepository.save(t);
            }
        }
        
        taskRepository.deleteById(id);
    }
}