package com.projectsia.taskmanager.task;

import com.projectsia.taskmanager.common.InvalidPaginationException;
import com.projectsia.taskmanager.common.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponse> findAll(Boolean completed, String search) {
        boolean hasSearch = hasSearch(search);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Task> tasks;

        if (completed != null && hasSearch) {
            tasks = taskRepository.findByCompletedAndTitleContainingIgnoreCase(completed, search, sort);
        } else if (completed != null) {
            tasks = taskRepository.findByCompleted(completed, sort);
        } else if (hasSearch) {
            tasks = taskRepository.findByTitleContainingIgnoreCase(search, sort);
        } else {
            tasks = taskRepository.findAll(sort);
        }

        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public PageResponse<TaskResponse> findPage(
            Boolean completed,
            String search,
            int page,
            int size
    ) {
        validatePagination(page, size);

        boolean hasSearch = hasSearch(search);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Task> taskPage;

        if (completed != null && hasSearch) {
            taskPage = taskRepository
                    .findByCompletedAndTitleContainingIgnoreCase(completed, search, pageable);
        } else if (completed != null) {
            taskPage = taskRepository.findByCompleted(completed, pageable);
        } else if (hasSearch) {
            taskPage = taskRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            taskPage = taskRepository.findAll(pageable);
        }

        return new PageResponse<>(
                taskPage.getContent().stream()
                        .map(taskMapper::toResponse)
                        .toList(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task savedTask = taskRepository.save(taskMapper.toEntity(request));

        log.info("Task created: id={}, title={}",
                savedTask.getId(),
                savedTask.getTitle());

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task existingTask = findEntityById(id);

        taskMapper.updateEntity(existingTask, request);

        Task savedTask = taskRepository.save(existingTask);

        log.info("Task updated: id={}, completed={}",
                savedTask.getId(),
                savedTask.isCompleted());

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public void delete(Long id) {
        Task existingTask = findEntityById(id);

        taskRepository.delete(existingTask);

        log.info("Task deleted: id={}", id);
    }

    private Task findEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private boolean hasSearch(String search) {
        return search != null && !search.isBlank();
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidPaginationException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(
                    "Size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
    }
}