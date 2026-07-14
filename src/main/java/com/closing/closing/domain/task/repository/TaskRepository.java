package com.closing.closing.domain.task.repository;

import com.closing.closing.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
