package com.projectsia.taskmanager.task;

import com.projectsia.taskmanager.common.CurrentUserService;
import com.projectsia.taskmanager.common.InvalidPaginationException;
import com.projectsia.taskmanager.common.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@Service
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;

    public TaskService(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.currentUserService = currentUserService;
    }

    public List<TaskResponse> findAll(Boolean completed, String search) {
        String ownerUsername = currentUserService.getUsername();

        Specification<Task> specification = buildSpecification(
                ownerUsername,
                completed,
                search
        );

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return taskRepository.findAll(specification, sort)
                .stream()
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

        String ownerUsername = currentUserService.getUsername();

        Specification<Task> specification = buildSpecification(
                ownerUsername,
                completed,
                search
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Task> taskPage = taskRepository.findAll(specification, pageable);

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
        String ownerUsername = currentUserService.getUsername();

        Task task = taskMapper.toEntity(request);
        task.setOwnerUsername(ownerUsername);

        Task savedTask = taskRepository.save(task);

        log.info("Task created: id={}, owner={}, title={}",
                savedTask.getId(),
                ownerUsername,
                savedTask.getTitle());

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task existingTask = findEntityById(id);

        taskMapper.updateEntity(existingTask, request);

        Task savedTask = taskRepository.save(existingTask);

        log.info("Task updated: id={}, owner={}, completed={}",
                savedTask.getId(),
                savedTask.getOwnerUsername(),
                savedTask.isCompleted());

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public void delete(Long id) {
        Task existingTask = findEntityById(id);

        taskRepository.delete(existingTask);

        log.info("Task deleted: id={}, owner={}",
                id,
                existingTask.getOwnerUsername());
    }

    private Task findEntityById(Long id) {
        String ownerUsername = currentUserService.getUsername();

        return taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private Specification<Task> buildSpecification(
            String ownerUsername,
            Boolean completed,
            String search
    ) {
        return TaskSpecifications.belongsTo(ownerUsername)
                .and(TaskSpecifications.completedEquals(completed))
                .and(TaskSpecifications.titleContains(search));
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