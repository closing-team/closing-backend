package com.closing.closing.domain.task.repository;

import com.closing.closing.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndUser_Id(Long taskId, Long userId);

    long countByUser_Id(Long userId);

    long countByUser_IdAndIsCompletedTrue(Long userId);

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.user.id = :userId
              AND t.startDate <= :endDate
              AND t.endDate >= :startDate
            ORDER BY t.startDate ASC, t.startTime ASC
            """)
    List<Task> findAllByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
