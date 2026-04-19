package com.main.taskmanager.web.model;

import com.main.taskmanager.task.model.enumclasses.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    private Priority priority;
}