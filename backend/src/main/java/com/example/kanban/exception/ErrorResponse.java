package com.example.kanban.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;          // HTTP 상태 코드 (예: 404)
    private String message;      // 에러 메시지
    private LocalDateTime timestamp; // 에러 발생 시간
}