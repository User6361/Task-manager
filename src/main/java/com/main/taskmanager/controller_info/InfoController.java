package com.main.taskmanager.controller_info;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.task.controller.TaskAssignFacade;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.task.service.TaskService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для обработки информационных страниц и сложных действий,
 * таких как массовое назначение, изменение приоритета и статуса задач.
 * Обеспечивает различные "вьюшки" для просмотра задач (открытые, завершенные, неназначенные).
 */
@Controller
@RequestMapping("/info")
@Slf4j
@RequiredArgsConstructor
public class InfoController {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskAssignFacade taskAssignFacade;


    // ======= Универсальный метод подготовки модели =======

    /**
     * Вспомогательный метод для подготовки базовых атрибутов модели, необходимых для большинства информационных страниц.
     * Включает список всех пользователей, текущего авторизованного пользователя и счетчик активных задач.
     *
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     */
    private void prepareBaseModel(Model model, User currentUser) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("countOfMyTasksMessage", getCountOfMyTasksMessage(taskService.getUserTasks(currentUser)));
    }


    // ================= POST - Универсальные действия =================

    /**
     * Универсальный POST-метод для Администратора.
     * Позволяет Администратору изменять назначение (assignee), приоритет (priority) и статус (status) задачи
     * из любого информационного представления.
     *
     * @param view Тип информационного представления (например, "open", "completed").
     * @param userId ID пользователя, чье представление просматривается.
     * @param taskId ID изменяемой задачи.
     * @param assigneeId Новый ID назначенного пользователя (опционально).
     * @param priority Новый приоритет задачи.
     * @param status Новый статус задачи.
     * @param currentUser Администратор, выполняющий действие.
     * @return Перенаправление обратно на текущее представление.
     */
    @PostMapping(value = "/{view}/{userId}/{taskId}/assign", params = {"assigneeId", "priority", "status"})
    @Loggable
    public String assignAdmin(@PathVariable String view,
                              @PathVariable Long userId,
                              @PathVariable Long taskId,
                              @RequestParam Optional<Long> assigneeId,
                              @RequestParam String priority,
                              @RequestParam String status,
                              @AuthenticationPrincipal User currentUser) {

        return taskAssignFacade.assignOrUpdateTask(
                taskId,
                assigneeId,
                priority,
                status,
                currentUser,
                "redirect:/info/" + view + "/" + userId
        );
    }

    /**
     * Удаляет задачу по ее ID.
     * Используется для представлений, которые отображают задачи конкретного пользователя.
     *
     * @param view Тип информационного представления.
     * @param taskId ID удаляемой задачи.
     * @param userId ID пользователя, чье представление просматривается.
     * @param currentUser Пользователь, выполняющий действие (должен иметь права).
     * @return Перенаправление обратно на представление пользователя.
     */
    @PostMapping(value="/{view}/{userId}/{taskId}/delete")
    public String deleteTask(@PathVariable String view,
                             @PathVariable Long taskId,
                             @PathVariable Long userId,
                             @AuthenticationPrincipal User currentUser) {

        taskService.deleteTaskById(taskId, currentUser);
        return "redirect:/info/" + view + "/" + userId;
    }

    /**
     * Удаляет задачу по ее ID.
     * Используется специально для представления "unassigned", которое не привязано к конкретному userId в URL.
     *
     * @param view Тип информационного представления ("unassigned").
     * @param taskId ID удаляемой задачи.
     * @param currentUser Пользователь, выполняющий действие (должен иметь права).
     * @return Перенаправление обратно на представление без пользователя.
     */
    @PostMapping(value = "/{view}/{taskId}/delete")
    public String deleteTask(@PathVariable String view,
                             @PathVariable Long taskId,
                             @AuthenticationPrincipal User currentUser){
        taskService.deleteTaskById(taskId, currentUser);
        return "redirect:/info/" + view;
    }

    /**
     * Универсальный POST-метод для обычного Пользователя.
     * Позволяет пользователю изменить ТОЛЬКО статус задачи (priority, assignee - не меняются).
     *
     * @param view Тип информационного представления.
     * @param userId ID пользователя, чье представление просматривается.
     * @param taskId ID изменяемой задачи.
     * @param status Новый статус задачи.
     * @param currentUser Пользователь, выполняющий действие.
     * @return Перенаправление обратно на текущее представление.
     */
    @PostMapping(value = "/{view}/{userId}/{taskId}/assign", params = "status")
    @Loggable
    public String assignUser(@PathVariable String view,
                             @PathVariable Long userId,
                             @PathVariable Long taskId,
                             @RequestParam String status,
                             @AuthenticationPrincipal User currentUser) {

        return taskAssignFacade.updateTaskStatusByUser(
                taskId,
                currentUser,
                status,
                "redirect:/info/" + view + "/" + userId
        );
    }


    // ================= GET — универсальные представления =================

    /**
     * Отображает список всех неназначенных (unassigned) задач в системе.
     * Доступно для просмотра Администратором.
     *
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Имя Thymeleaf-шаблона для неназначенных задач ("info/info-unassigned").
     */
    @GetMapping("/unassigned")
    public String unassignTasks(Model model, @AuthenticationPrincipal User currentUser) {
        List<Task> unassignedTasks = taskService.findAllTasks().stream()
                .filter(t -> t.getAssignee() == null)
                .toList();

        prepareBaseModel(model, currentUser);
        model.addAttribute("tasks",  unassignedTasks);
        model.addAttribute("user",  currentUser);
        return "info/info-unassigned";
    }

    /**
     * Универсальный метод для отображения задач конкретного пользователя по заданному типу представления (view).
     * Обрабатывает "open", "completed", "in-progress", "total".
     *
     *
     * @param view Тип фильтрации задач (например, "open" или "completed").
     * @param userId ID пользователя, задачи которого запрашиваются.
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Имя Thymeleaf-шаблона в формате "info/info-{view}".
     * @throws IllegalArgumentException Если передан неизвестный тип представления.
     */
    @GetMapping("/{view}/{userId}")
    @Loggable
    public String getTasksByView(@PathVariable String view,
                                 @PathVariable Long userId,
                                 Model model,
                                 @AuthenticationPrincipal User currentUser) {

        prepareBaseModel(model, currentUser);

        User targetUser = userService.getUserById(userId).orElseThrow();
        model.addAttribute("user", targetUser);

        // Логика фильтрации задач в зависимости от требуемого представления (view)
        List<Task> tasks = switch (view) {
            case "open" -> taskService.getUserTasks(targetUser).stream()
                    .filter(t -> t.getStatus() == TaskStatus.OPEN).toList();

            case "completed" -> taskService.getUserTasks(targetUser).stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED).toList();

            case "in-progress" -> taskService.getUserTasks(targetUser).stream()
                    .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).toList();

            case "total" -> taskService.getUserTasks(targetUser);
            case "unassigned" -> taskService.getUserTasks(targetUser).stream().filter(t -> t.getAssignee() == null).toList();

            default -> throw new IllegalArgumentException("Unknown view type: " + view);
        };

        model.addAttribute("tasks", tasks);

        return "info/info-" + view;
    }


    // ================= Корневой /info =================

    /**
     * Отображает главную информационную страницу ("/info"), которая, вероятно,
     * содержит сводную статистику по всем задачам и пользователям.
     *
     * @param model Модель Spring UI.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Имя Thymeleaf-шаблона для корневой информационной страницы ("info/info").
     */
    @GetMapping
    @Loggable
    public String getInfo(Model model, @AuthenticationPrincipal User currentUser) {
        prepareBaseModel(model, currentUser);

        List<Task> unassigned = taskService.findAllTasks().stream()
                .filter(t -> t.getAssignee() == null)
                .toList();

        model.addAttribute("tasks", taskService.findAllTasks());
        model.addAttribute("unassigneTasks", unassigned);

        return "info/info";
    }


    // ================= Вспомогательные подсчёты =================

    /**
     * Подсчитывает количество задач с высоким (HIGH) или срочным (URGENT) приоритетом в заданном списке.
     *
     * @param tasks Список задач.
     * @return Количество задач с высоким или срочным приоритетом.
     */
    @Loggable
    public long getCountOfUserTasksWithHighPriority(List<Task> tasks) {
        return tasks.stream()
                .filter(t -> t.getPriority() == Priority.HIGH || t.getPriority() == Priority.URGENT)
                .count();
    }

    /**
     * Подсчитывает количество активных задач (все, кроме COMPLETED) для отображения в качестве сообщения/уведомления.
     *
     * @param tasks Список задач текущего пользователя.
     * @return Количество незавершенных задач.
     */
    @Loggable
    public long getCountOfMyTasksMessage(List<Task> tasks) {
        return tasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED).count();
    }
}