package com.main.taskmanager.task.controller;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.service.TaskService;
import com.main.taskmanager.task.web.model.AllTasksResponse;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.service.UserService;
import com.main.taskmanager.web.model.CreateTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskControllerApi {
    private final TaskService taskService;
    private final UserService userService;
    private final TaskAssignFacade taskAssignFacade;


    @PostMapping("/{taskId}/delete")
    @Loggable
    public String deleteTask(@PathVariable Long taskId, @AuthenticationPrincipal User currentUser) {
        return null;
    }


    @GetMapping(value = "/find-tasks/{taskId}")
    @Loggable
    public String findTaskById(@PathVariable Long taskId) {

        return null;
    }



    @GetMapping
    @Loggable

    public Flux<ResponseEntity<AllTasksResponse>> getAllTasks() {
        return null;
    }


    @GetMapping("/my-tasks")
    @Loggable
    public String getMyTasks(@AuthenticationPrincipal User currentUser, Model model) {
        return null;
    }



    @PostMapping()
    @Loggable
    public String saveTask(CreateTaskRequest taskRequest, @AuthenticationPrincipal User currentUser) {
        return null;
    }



}
