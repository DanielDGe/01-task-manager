package com.projectsia.taskmanager.task;

import com.projectsia.taskmanager.common.InvalidPaginationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, new TaskMapper());
    }

    @Test
    void shouldCreateTask() {
        TaskRequest request = new TaskRequest(
                "Write automated tests",
                "Test TaskService with Mockito",
                false
        );

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(10L);
            task.setCreatedAt(LocalDateTime.of(2026, 7, 3, 12, 0));
            task.setUpdatedAt(LocalDateTime.of(2026, 7, 3, 12, 0));
            return task;
        });

        TaskResponse response = taskService.create(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        assertThat(taskCaptor.getValue().getTitle()).isEqualTo("Write automated tests");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Write automated tests");
        assertThat(response.completed()).isFalse();
    }

    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 999");
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> taskService.findPage(null, null, -1, 5))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessage("Page must be greater than or equal to 0");

        assertThatThrownBy(() -> taskService.findPage(null, null, 0, 101))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessage("Size must be between 1 and 100");
    }
}