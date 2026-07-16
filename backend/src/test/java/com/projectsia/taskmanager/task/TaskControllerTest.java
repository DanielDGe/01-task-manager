package com.projectsia.taskmanager.task;

import com.projectsia.taskmanager.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private TaskService taskService;

        @Test
        void shouldCreateTaskAndReturn201() throws Exception {
                
                TaskResponse response = new TaskResponse(
                                7L,
                                "Controller test",
                                "Testing POST endpoint",
                                false,
                                "daniel",
                                LocalDateTime.of(2026, 7, 3, 12, 0),
                                LocalDateTime.of(2026, 7, 3, 12, 0));

                given(taskService.create(any(TaskRequest.class))).willReturn(response);

                mockMvc.perform(post("/api/tasks")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "title": "Controller test",
                                                  "description": "Testing POST endpoint",
                                                  "completed": false
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", "/api/tasks/7"))
                                .andExpect(jsonPath("$.id").value(7))
                                .andExpect(jsonPath("$.title").value("Controller test"));

                verify(taskService).create(any(TaskRequest.class));
        }

        @Test
        void shouldReturn400WhenTitleIsBlank() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "title": "",
                                                  "description": "Invalid request",
                                                  "completed": false
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.messages[0]")
                                                .value("title: Title is required"));
        }

        @Test
        void shouldReturn404WhenTaskDoesNotExist() throws Exception {
                given(taskService.findById(999L))
                                .willThrow(new TaskNotFoundException(999L));

                mockMvc.perform(get("/api/tasks/999")
                                .with(jwt()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                                .andExpect(jsonPath("$.messages[0]")
                                                .value("Task not found with id: 999"));
        }
}