package com.example.kanban.exception;

// 특정 태스크를 찾을 수 없을 때 발생하는 예외
public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(String message) {
        super(message); // 전달받은 메시지를 부모 클래스(RuntimeException)에 저장
    }
}