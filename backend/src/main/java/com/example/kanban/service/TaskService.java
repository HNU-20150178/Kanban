package com.example.kanban.service;

import com.example.kanban.dto.TaskDTO;
import com.example.kanban.entity.Task;
import com.example.kanban.entity.Task.TaskStatus;
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
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: " + id));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setAssignee(taskDTO.getAssignee());
        task.setPriority(taskDTO.getPriority());

        return TaskDTO.fromEntity(taskRepository.save(task));
    }

    // 업무 이동 (드래그앤드롭)
    public void moveTask(Long taskId, TaskStatus newStatus, Integer newPosition) {
        Task taskToMove = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: " + taskId));

        TaskStatus oldStatus = taskToMove.getStatus();

        // 기존 컬럼에서 Task를 제거하고 남은 Task들의 position을 재정렬
        List<Task> oldColumnTasks = taskRepository.findByStatusOrderByPositionAsc(oldStatus);

        // 이동할 Task를 oldColumnTasks에서 제외하고 재정렬
        List<Task> filteredOldColumnTasks = new ArrayList<>();
        for (Task t : oldColumnTasks) {
            if (!Objects.equals(t.getId(), taskId)) { // 이동할 Task 본인을 제외 (Objects.equals로 null-safe 비교)
                filteredOldColumnTasks.add(t);
            }
        }

        // 필터링된 리스트의 position을 0부터 순서대로 다시 할당
        for (int i = 0; i < filteredOldColumnTasks.size(); i++) {
            Task t = filteredOldColumnTasks.get(i);
            if (!Objects.equals(t.getPosition(), i)) { // position이 실제로 변경될 때만 저장
                t.setPosition(i);
                taskRepository.save(t);
            }
        }

        // 새 컬럼에 Task를 삽입하고 해당 컬럼의 Task들의 position을 재정렬
        List<Task> newColumnTasks = taskRepository.findByStatusOrderByPositionAsc(newStatus);

        /*
         * 이동할 Task를 새 리스트의 newPosition에 임시로 삽입하여 전체 재정렬을 준비
         * (만약 이동하는 Task가 이미 newColumnTasks에 포함되어 있다면 먼저 제거)
         */
        newColumnTasks.removeIf(t -> Objects.equals(t.getId(), taskId));

        // newPosition이 리스트의 크기를 벗어나는 경우 (맨 뒤로 이동)
        if (newPosition > newColumnTasks.size()) {
            newPosition = newColumnTasks.size();
        }

        // 새 위치에 TaskToMove가 들어갈 공간을 확보 실제 삽입은 나중에
        newColumnTasks.add(newPosition, taskToMove); // 여기에 taskToMove를 임시로 추가하여 position 계산

        // 전체 newColumnTasks의 position을 0부터 순서대로 다시 할당
        for (int i = 0; i < newColumnTasks.size(); i++) {
            Task t = newColumnTasks.get(i);

            if (!Objects.equals(t.getPosition(), i)) { // position이 실제로 변경될 때만 저장
                t.setPosition(i);
                taskRepository.save(t); // 변경된 position 저장
            }
        }

        /*
         * 이동하는 TaskToMove의 최종 상태 및 위치를 업데이트 (이미 위에서 저장되었을 수 있지만, 명확성을 위해 다시 한번)
         * 위에서 newColumnTasks에 taskToMove를 넣어 position을 결정하고 save했으므로, 이 부분은 생략 가능하거나
         * 변경된 status를 확정하는 정도로만 사용
         */
        if (!Objects.equals(taskToMove.getStatus(), newStatus)) { // status가 변경되었을 때만
            taskToMove.setStatus(newStatus);
            taskRepository.save(taskToMove); // status 변경 사항 저장
        }
    }

    // 업무 삭제
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: " + id));

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
}