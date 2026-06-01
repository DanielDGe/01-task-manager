package com.projectsia.taskmanager.task;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAll()
                .stream()
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
                task.isCompleted()
        );
    }
}