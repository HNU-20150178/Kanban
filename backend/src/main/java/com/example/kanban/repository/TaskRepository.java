package com.example.kanban.repository;

import com.example.kanban.entity.Task;
import com.example.kanban.entity.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByStatusOrderByPositionAsc(TaskStatus status);
    
    List<Task> findAllByOrderByStatusAscPositionAsc();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.position = t.position - 1 " +
           "WHERE t.status = :status AND t.position > :position")
    void decrementPositionsAfter(
            @Param("status") TaskStatus status,
            @Param("position") Integer position);
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.position = t.position - 1 " +
           "WHERE t.status = :status " +
           "AND t.position >= :start AND t.position <= :end")
    void decrementPositionsBetween(
            @Param("status") TaskStatus status,
            @Param("start") Integer start,
            @Param("end") Integer end);
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.position = t.position + 1 " +
           "WHERE t.status = :status AND t.position >= :position")
    void incrementPositionsFrom(
            @Param("status") TaskStatus status,
            @Param("position") Integer position);
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.position = t.position + 1 " +
           "WHERE t.status = :status " +
           "AND t.position >= :start AND t.position <= :end")
    void incrementPositionsBetween(
            @Param("status") TaskStatus status,
            @Param("start") Integer start,
            @Param("end") Integer end);
}