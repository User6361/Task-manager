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

/**
 * Компонент-Фасад для инкапсуляции и упрощения сложной бизнес-логики,
 * связанной с назначением, снятием назначения и обновлением задач.
 * Обеспечивает централизованную проверку прав доступа перед выполнением операций.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskAssignFacade {

    private final TaskService taskService;

    /**
     * Основной метод для изменения назначения, приоритета и/или статуса задачи.
     * <p>
     * <b>Логика доступа:</b>
     * <ul>
     * <li>ADMINISTRATOR: Может изменять любые параметры любой задачи.</li>
     * <li>USER: Может изменять только параметры задачи, назначенной ему, но только если этот метод используется
     * через специализированный путь (например, {@code updateTaskStatusByUser}).</li>
     * </ul>
     * Если {@code assigneeId} отсутствует, происходит снятие назначения (unassign).
     *
     * @param taskId ID изменяемой задачи.
     * @param assigneeId Опциональный ID нового назначенного пользователя (если пуст, задача снимается с назначения).
     * @param priority Новый приоритет задачи.
     * @param status Новый статус задачи.
     * @param currentUser Пользователь, инициировавший изменение.
     * @param redirectPath Путь для перенаправления в случае успешного выполнения или недостаточных прав.
     * @return Путь для перенаправления (String).
     */
    @Loggable
    public String assignOrUpdateTask(Long taskId,
                                     Optional<Long> assigneeId,
                                     String priority,
                                     String status,
                                     User currentUser,
                                     String redirectPath){

        // Проверка прав для обычных пользователей
        if (!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)) {
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

    /**
     * Специализированный метод для изменения ТОЛЬКО статуса задачи обычным пользователем.
     * Выполняет строгую проверку, что задача назначена именно этому пользователю.
     *
     * @param taskId ID изменяемой задачи.
     * @param currentUser Пользователь, инициировавший изменение.
     * @param status Новый статус задачи.
     * @param redirectPath Путь для перенаправления.
     * @return Путь для перенаправления (String).
     */
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

    /**
     * Вспомогательный метод для подсчета количества незавершенных задач.
     * Используется для отображения уведомлений в UI.
     *
     * @param userTasks Список задач пользователя.
     * @return Количество задач со статусом, отличным от COMPLETED.
     */
    public long getCountOfMyTasksMessage(List<Task> userTasks){
        return userTasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .count();
    }
}