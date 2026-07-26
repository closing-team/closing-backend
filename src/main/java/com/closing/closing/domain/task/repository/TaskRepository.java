package com.closing.closing.domain.task.repository;

import com.closing.closing.domain.business.entity.BusinessRegistration;
import com.closing.closing.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT br FROM BusinessRegistration br WHERE br.user.id = :userId")
    Optional<BusinessRegistration> findBusinessRegistrationByUserId(
            @Param("userId") Long userId
    );

    Optional<Task> findByIdAndRegistration_User_Id(Long taskId, Long userId);

    long countByRegistration_User_Id(Long userId);

    long countByRegistration_User_IdAndIsCompletedTrue(Long userId);

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.registration.user.id = :userId
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
