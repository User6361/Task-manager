package com.main.taskmanager.task.controller;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.task.service.TaskService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.model.enumclasses.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class TaskAssignFacade {

    private final TaskService taskService;
    @Loggable
    public String assignOrUpdateTask(Long taskId,
                                     Optional<Long> assigneeId,
                                     String priority,
                                     String status,
                                     User currentUser,
                                     String redirectPath){

        // Проверка прав для обычных пользователей
        if (!currentUser.getRole().equals(Role.ADMINISTRATOR)) {
            Optional<Task> optionalTask = taskService.getTaskById(taskId);
            // Если задача не найдена или пользователь не назначен, запрещаем операцию
            if (optionalTask.isEmpty() || !optionalTask.get().getAssignee().getId().equals(currentUser.getId())) {
                log.warn("User {} (ID: {}) attempted to assign a task (ID: {}) without admin rights or being the assignee.",
                        currentUser.getUsername(), currentUser.getId(), taskId);
                return redirectPath;
            }
        }

        // Логика назначения/снятия назначения
        if (assigneeId.isPresent()) {
            log.info("Assigning/Updating task ID {} by user {}.", taskId, currentUser.getUsername());
            taskService.assignTask(taskId, assigneeId.get(), Priority.valueOf(priority), TaskStatus.valueOf(status), currentUser);
        } else {
            log.info("Unassigning task ID {} by user {}.", taskId, currentUser.getUsername());
            taskService.unassignTask(taskId, currentUser);
        }

        return redirectPath;
    }

    @Loggable
    public String updateTaskStatusByUser(Long taskId,
                                         User currentUser,
                                         String status,
                                         String redirectPath) {

        Optional<Task> optionalTask = taskService.getTaskById(taskId);

        if (optionalTask.isEmpty()) {
            log.error("Task with ID {} not found during status update.", taskId);
            return redirectPath;
        }

        Task task = optionalTask.get();

        // Проверка: задача должна быть назначена текущему пользователю
        if (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to change status of task ID {} which is not assigned to them.", currentUser.getUsername(), taskId);
            return redirectPath;
        }

        // Получаем текущие данные, которые не меняются (assignee, priority)
        Long assigneeId = task.getAssignee().getId();
        Priority priorityFromTask = task.getPriority();
        log.info("Updating task ID {} status (by user {}) to {}.", taskId, currentUser.getUsername(), status);

        try {
            // Используем основной метод taskService.assignTask, передавая неизменные данные, кроме статуса
            taskService.assignTask(taskId, assigneeId, priorityFromTask, TaskStatus.valueOf(status), currentUser);
        } catch (NoRightsException e) {
            // В данном контексте NoRightsException маловероятен, но обрабатывается для полноты
            log.warn("No rights exception during status update for task ID {}.", taskId);
        } catch (Exception e) {
            log.error("Error updating status for task ID {}: {}", taskId, e.getMessage(), e);
        }

        return redirectPath;
    }

    public long getCountOfMyTasksMessage(List<Task> userTasks){
        return userTasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .count();
    }
}