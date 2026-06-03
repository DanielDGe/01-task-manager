package com.projectsia.taskmanager.task;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import com.projectsia.taskmanager.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> findAll(Boolean completed, String search) {
        boolean hasSearch = search != null && !search.isBlank();

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
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse findById(Long id) {
        Task task = findEntityById(id);
        return toResponse(task);
    }

    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(request.completed());

        return toResponse(taskRepository.save(task));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task existingTask = findEntityById(id);

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setCompleted(request.completed());

        return toResponse(taskRepository.save(existingTask));
    }

    public void delete(Long id) {
        Task existingTask = findEntityById(id);
        taskRepository.delete(existingTask);
    }

    private Task findEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    public PageResponse<TaskResponse> findPage(Boolean completed, String search, int page, int size) {
        boolean hasSearch = search != null && !search.isBlank();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Task> taskPage;

        if (completed != null && hasSearch) {
            taskPage = taskRepository.findByCompletedAndTitleContainingIgnoreCase(completed, search, pageable);
        } else if (completed != null) {
            taskPage = taskRepository.findByCompleted(completed, pageable);
        } else if (hasSearch) {
            taskPage = taskRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            taskPage = taskRepository.findAll(pageable);
        }

        return new PageResponse<>(
                taskPage.getContent().stream().map(this::toResponse).toList(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast());
    }

}