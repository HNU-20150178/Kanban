package com.example.kanban.dto;

import com.example.kanban.entity.Task.TaskStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveTaskRequest {
    @NotNull(message = "상태는 필수입니다")
    private TaskStatus status;
    
    @NotNull(message = "위치는 필수입니다")
    @Min(value = 0, message = "위치는 0 이상이어야 합니다")
    private Integer position;
}
