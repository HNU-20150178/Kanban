package com.example.kanban.exception;

// 특정 업무를 찾을 수 없을 때 발생하는 예외
public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(Long id) {
        super("업무를 찾을 수 없습니다. ID: " + id);
    }
}