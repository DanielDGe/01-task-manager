package com.projectsia.taskmanager.task;

import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> belongsTo(String ownerUsername) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ownerUsername"), ownerUsername);
    }

    public static Specification<Task> completedEquals(Boolean completed) {
        return (root, query, criteriaBuilder) -> {
            if (completed == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("completed"), completed);
        };
    }

    public static Specification<Task> titleContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + search.toLowerCase() + "%"
            );
        };
    }
}