package com.closing.closing.domain.task.repository;

import com.closing.closing.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.startDate <= :endDate AND t.endDate >= :startDate ORDER BY t.startDate ASC, t.startTime ASC")
    List<Task> findAllByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
