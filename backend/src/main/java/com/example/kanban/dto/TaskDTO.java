package com.example.kanban.dto;

import com.example.kanban.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {
    
    private Long id;
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    private String description;
    
    @NotNull(message = "상태는 필수입니다")
    private Task.TaskStatus status;
    
    @NotNull(message = "위치는 필수입니다")
    private Integer position;
    
    private String assignee;
    
    private Task.Priority priority;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static TaskDTO fromEntity(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .position(task.getPosition())
                .assignee(task.getAssignee())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    
    public Task toEntity() {
        return Task.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .status(this.status)
                .position(this.position)
                .assignee(this.assignee)
                .priority(this.priority)
                .build();
    }
}