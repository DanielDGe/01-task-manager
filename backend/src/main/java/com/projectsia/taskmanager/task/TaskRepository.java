package com.projectsia.taskmanager.task;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCompleted(boolean completed, Sort sort);

    List<Task> findByTitleContainingIgnoreCase(String title, Sort sort);

    List<Task> findByCompletedAndTitleContainingIgnoreCase(boolean completed, String title, Sort sort);
}