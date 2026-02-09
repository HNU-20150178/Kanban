package com.example.kanban.service;

import com.example.kanban.dto.TaskDTO;
import com.example.kanban.entity.Task;
import com.example.kanban.entity.Task.TaskStatus;
import com.example.kanban.exception.TaskNotFoundException;
import com.example.kanban.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    // 모든 업무 조회
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAllByOrderByStatusAscPositionAsc()
                .stream()
                .map(TaskDTO::fromEntity)
                .toList();
    }

    // 상태별 업무 조회
    public List<TaskDTO> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByPositionAsc(status)
                .stream()
                .map(TaskDTO::fromEntity)
                .toList();
    }

    // 업무 생성
    public TaskDTO createTask(TaskDTO taskDTO) {
        // 같은 상태의 마지막 위치 구하기
        List<Task> tasksInSameStatus = taskRepository.findByStatusOrderByPositionAsc(taskDTO.getStatus());
        int newPosition = tasksInSameStatus.isEmpty() ? 0 : tasksInSameStatus.size();

        taskDTO.setPosition(newPosition);
        Task task = taskRepository.save(taskDTO.toEntity());
        return TaskDTO.fromEntity(task);
    }

    // 업무 수정
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setAssignee(taskDTO.getAssignee());
        task.setPriority(taskDTO.getPriority());

        return TaskDTO.fromEntity(taskRepository.save(task));
    }

    // 업무 이동 (드래그앤드롭)
    public void moveTask(Long taskId, TaskStatus newStatus, Integer newPosition) {
        // 1. Task 조회
        Task taskToMove = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        TaskStatus oldStatus = taskToMove.getStatus();
        Integer oldPosition = taskToMove.getPosition();
        
        // 2. 같은 위치일 경우 리턴
        if (oldStatus.equals(newStatus) && oldPosition.equals(newPosition)) {
            return;
        }
        
        // 3. 같은 컬럼 내 이동
        if (oldStatus.equals(newStatus)) {
            moveWithinSameColumn(taskToMove, oldPosition, newPosition);
            return;
        }
        
        // 4. 다른 컬럼으로 이동
        moveToDifferentColumn(taskToMove, oldStatus, newStatus, oldPosition, newPosition);
    }

    // 업무 삭제
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // 같은 상태의 다른 업무들 위치 조정
        List<Task> tasksInSameStatus = taskRepository.findByStatusOrderByPositionAsc(task.getStatus());
        // 삭제할 Task를 리스트에서 제외하고 재정렬
        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : tasksInSameStatus) {
            if (!Objects.equals(t.getId(), id)) {
                filteredTasks.add(t);
            }
        }

        // 재정렬된 리스트의 position을 0부터 순서대로 다시 할당
        for (int i = 0; i < filteredTasks.size(); i++) {
            Task t = filteredTasks.get(i);
            if (!Objects.equals(t.getPosition(), i)) {
                t.setPosition(i);
                taskRepository.save(t);
            }
        }

        taskRepository.deleteById(id);
    }

    // 업무 이동 (드래그앤드롭)의 같은 컬럼 이동의 경우
    private void moveWithinSameColumn(Task task, Integer oldPos, Integer newPos) {
        if (oldPos < newPos) {
            // 아래로 이동: [oldPos+1, newPos] 범위 -1
            taskRepository.decrementPositionsBetween(
                task.getStatus(), oldPos + 1, newPos
            );
        } else {
            // 위로 이동: [newPos, oldPos-1] 범위 +1
            taskRepository.incrementPositionsBetween(
                task.getStatus(), newPos, oldPos - 1
            );
        }
        
        task.setPosition(newPos);
        taskRepository.save(task);
    }

    // 업무 이동 (드래그앤드롭)의 다른 컬럼 이동의 경우
    private void moveToDifferentColumn(
            Task task, 
            TaskStatus oldStatus, 
            TaskStatus newStatus,
            Integer oldPosition,
            Integer newPosition) {
        
        // 1. 기존 컬럼: 뒤 Task들 -1
        taskRepository.decrementPositionsAfter(oldStatus, oldPosition);
        
        // 2. 새 컬럼: newPosition 이후 +1
        taskRepository.incrementPositionsFrom(newStatus, newPosition);
        
        // 3. Task 업데이트
        task.setStatus(newStatus);
        task.setPosition(newPosition);
        taskRepository.save(task);
    }
}