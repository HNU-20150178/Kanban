package com.example.kanban.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TASKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "TASK_SEQ", allocationSize = 1)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;
    
    @Column(nullable = false)
    private Integer position;
    
    @Column(length = 50)
    private String assignee;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TaskStatus {
        TODO, IN_PROGRESS, DONE
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}