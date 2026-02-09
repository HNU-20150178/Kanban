package com.example.kanban.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldErrorDetail> errors; // 상세 에러 목록

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;   // 에러가 발생한 필드명 (예: "title")
        private String message; // 에러 내용 (예: "상태는 필수입니다")
    }
}