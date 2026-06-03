package com.projectsia.taskmanager.task;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCompleted(boolean completed, Sort sort);

    List<Task> findByTitleContainingIgnoreCase(String title, Sort sort);

    List<Task> findByCompletedAndTitleContainingIgnoreCase(boolean completed, String title, Sort sort);

    Page<Task> findByCompleted(boolean completed, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Task> findByCompletedAndTitleContainingIgnoreCase(boolean completed, String title, Pageable pageable);
}