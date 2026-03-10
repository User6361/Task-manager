package com.main.taskmanager.task.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllTasksResponse {
    private List<TaskResponse> allTasks;

}
