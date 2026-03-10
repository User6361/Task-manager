package com.main.taskmanager.task.web.model;

import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private Long assigneeId;
    private String assigneeName;
    private String createdAt;
    private String updatedAt;
}