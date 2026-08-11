package com.closing.closing.domain.task.entity;

import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(nullable = false)
    private boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskSource source = TaskSource.MANUAL;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    public Task(User user, String title, LocalDate startDate,
                LocalDate endDate, LocalTime startTime, LocalTime endTime, TaskSource source, String description) {
        this.user = user;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.source = source;
        this.description = description;
    }

    public void update(String title, LocalDate startDate, LocalDate endDate,
                       LocalTime startTime, LocalTime endTime, String description) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (startTime != null) {
            this.startTime = startTime;
        }
        if (endTime != null) {
            this.endTime = endTime;
        }
        if (description != null) {
            this.description = description;
        }
    }

    public void complete(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

}
