package com.projectsia.taskmanager.task;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
    return taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task create(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(request.completed());

        return taskRepository.save(task);
    }

    public Task update(Long id, TaskRequest request) {
        Task existingTask = findById(id);

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setCompleted(request.completed());

        return taskRepository.save(existingTask);
    }

    public void delete(Long id) {
        Task existingTask = findById(id);
        taskRepository.delete(existingTask);
    }
}