package com.main.taskmanager.task.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllTasksResponse {
    private List<TaskResponse> allTasks;

}
