package com.projectsia.taskmanager.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must have at most 100 characters")
        String title,

        @Size(max = 255, message = "Description must have at most 255 characters")
        String description,

        boolean completed
) {
}