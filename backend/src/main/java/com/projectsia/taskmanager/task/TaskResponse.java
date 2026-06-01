package com.projectsia.taskmanager.task;

public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed
) {
}