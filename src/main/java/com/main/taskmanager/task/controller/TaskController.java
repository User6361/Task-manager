package com.main.taskmanager.task.controller;

import com.main.taskmanager.aop.Loggable;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.task.service.TaskService;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.user.service.UserService;
import com.main.taskmanager.web.model.CreateTaskRequest;    
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.user.model.enumclasses.Role;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для обработки всех основных операций, связанных с задачами (CRUD).
 * Предоставляет конечные точки для просмотра, создания, удаления и назначения/обновления задач
 * с учетом ролей и различных представлений (все задачи, мои задачи, поиск).
 */
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskAssignFacade taskAssignFacade;

    private static final String REDIRECT_ALL_TASKS = "redirect:/tasks";
    private static final String REDIRECT_MY_TASKS = "redirect:/tasks/my-tasks";

    // ================= HELPER METHOD =================

    /**
     * Добавляет базовую информацию о пользователе, список пользователей и счетчик активных задач в модель.
     *
     * @param currentUser Текущий авторизованный пользователь.
     * @param model Модель Spring UI.
     */
    private void prepareBaseModel(User currentUser, Model model) {
        List<Task> userTasks = taskService.getUserTasks(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("countOfMyTasksMessage", taskAssignFacade.getCountOfMyTasksMessage(userTasks));
    }


    // ================= DELETE METHODS =================

    /**
     * Удаляет задачу из представления "Мои задачи".
     * Требует роли {@link Role#ADMINISTRATOR}.
     *
     * @param taskId ID удаляемой задачи.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Перенаправление на "/tasks/my-tasks".
     */
    @PostMapping("/my-tasks/{taskId}/delete")
    @Loggable
    public String deleteTaskMyTasks(@PathVariable Long taskId, @AuthenticationPrincipal User currentUser) {
        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)){
            log.info("User don't have permission to delete task");
            return REDIRECT_MY_TASKS;
        }
        log.info("Deleting task - " + taskId);
        taskService.deleteTaskById(taskId, currentUser);
        return REDIRECT_MY_TASKS;
    }

    /**
     * Удаляет задачу из представления "Все задачи".
     * Требует роли {@link Role#ADMINISTRATOR}.
     *
     * @param taskId ID удаляемой задачи.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Перенаправление на "/tasks".
     */
    @PostMapping("/{taskId}/delete")
    @Loggable
    public String deleteTask(@PathVariable Long taskId, @AuthenticationPrincipal User currentUser) {
        if(!currentUser.getRoles().stream().anyMatch(Role.ADMINISTRATOR::equals)){
            log.info("User don't have permission to delete task");
            return REDIRECT_ALL_TASKS;
        }
        log.info("Deleting task - " + taskId);
        taskService.deleteTaskById(taskId, currentUser);
        return REDIRECT_ALL_TASKS;
    }

    /**
     * Удаляет задачу со страницы поиска по ID.
     *
     * @param taskId ID удаляемой задачи.
     * @param currentUser Текущий авторизованный пользователь.
     * @return Перенаправление обратно на страницу поиска с тем же ID.
     */
    @PostMapping("/find-tasks/{taskId}/delete")
    @Loggable
    public String deleteTaskFindTasks(@PathVariable Long taskId,@AuthenticationPrincipal User currentUser) {
        log.info("Deleting task - " + taskId);
        taskService.deleteTaskById(taskId, currentUser);
        return "redirect:/tasks/find-tasks/" + taskId;
    }

    // ================= ASSIGNEE TASK (USER) METHODS =================

    /**
     * Обновление СТАТУСА задачи обычным пользователем на странице "Все задачи".
     */
    @PostMapping(value = "/{taskId}/assign", params = "status")
    public String assignTaskNoAdmin(@PathVariable Long taskId, @RequestParam String status, @AuthenticationPrincipal User currentUser) {
        return taskAssignFacade.updateTaskStatusByUser(taskId, currentUser, status, REDIRECT_ALL_TASKS);
    }

    /**
     * Обновление СТАТУСА задачи обычным пользователем на странице "Мои задачи".
     */
    @PostMapping(value = "/my-tasks/{taskId}/assign", params = "status")
    public String assignTaskMyTasksNoAdmin(@PathVariable Long taskId, @RequestParam String status, @AuthenticationPrincipal User currentUser) {
        return taskAssignFacade.updateTaskStatusByUser(taskId, currentUser, status, REDIRECT_MY_TASKS);
    }

    /**
     * Обновление СТАТУСА задачи обычным пользователем на странице поиска.
     */
    @PostMapping(value ="find-tasks/{taskId}/assign", params = "status")
    public String assignTaskFindTasksNoAdmin(@PathVariable Long taskId, @RequestParam String status, @AuthenticationPrincipal User currentUser){
        taskAssignFacade.updateTaskStatusByUser(taskId, currentUser, status, null);
        return "redirect:/tasks/find-tasks/" + taskId;
    }


    // ================= ASSIGNEE TASK (ADMIN/GENERAL) METHODS =================

    /**
     * Обновление задачи (assignee, priority, status) из представления "Все задачи".
     * Использует фасад для инкапсуляции логики прав.
     */
    @PostMapping(value =  "/{taskId}/assign", params ={"assigneeId","priority","status"} )
    @Loggable
    public String assignTask(@PathVariable Long taskId,
                             @RequestParam Optional<Long> assigneeId,
                             @RequestParam String priority,
                             @RequestParam String status,
                             @AuthenticationPrincipal User currentUser) {
        return taskAssignFacade.assignOrUpdateTask(taskId, assigneeId, priority, status, currentUser, REDIRECT_ALL_TASKS);
    }

    /**
     * Обновление задачи (assignee, priority, status) из представления "Мои задачи".
     */
    @PostMapping(value = "/my-tasks/{taskId}/assign",  params ={"assigneeId","priority","status"})
    @Loggable
    public String assignTaskMyTasks(@PathVariable Long taskId,
                                    @RequestParam Optional<Long> assigneeId,
                                    @RequestParam String priority,
                                    @RequestParam String status,
                                    @AuthenticationPrincipal User currentUser) {
        return taskAssignFacade.assignOrUpdateTask(taskId, assigneeId, priority, status, currentUser, REDIRECT_MY_TASKS);
    }

    /**
     * Обновление задачи (assignee, priority, status) со страницы поиска.
     */
    @PostMapping(value ="find-tasks/{taskId}/assign", params={"assigneeId","priority","status"})
    public String assignTaskFindTasks(@PathVariable Long taskId,
                                      @RequestParam Optional<Long> assigneeId,
                                      @RequestParam String priority,
                                      @RequestParam String status,
                                      @AuthenticationPrincipal User currentUser){
        taskAssignFacade.assignOrUpdateTask(taskId, assigneeId, priority, status, currentUser, null);
        return "redirect:/tasks/find-tasks/" + taskId;
    }


    // ================= FIND VIEW METHODS =================

    /**
     * Обрабатывает запрос на поиск задачи по ID.
     *
     * @param taskId ID искомой задачи.
     * @param currentUser Текущий пользователь.
     * @param model Модель Spring UI.
     * @return Имя Thymeleaf-шаблона для страницы поиска.
     */
    @GetMapping(value = "/find-tasks/{taskId}")
    @Loggable
    public String findTaskById(@PathVariable Long taskId,
                               @AuthenticationPrincipal User currentUser,
                               Model model) {

        prepareBaseModel(currentUser, model);

        if (taskService.existsById(taskId)) {
            Task task = taskService.getTaskById(taskId).get();
            model.addAttribute("task", task);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("searchFound", true);
        } else {
            model.addAttribute("searchPerformed", true);
            model.addAttribute("searchFound", false);
            model.addAttribute("searchedId", taskId);
        }

        return "tasks/find-tasks";
    }

    /**
     * Отображает пустую страницу поиска задач.
     */
    @GetMapping("/find-tasks")
    @Loggable
    public String findTaskPage(@AuthenticationPrincipal User currentUser,
                               Model model) {

        prepareBaseModel(currentUser, model);

        return "tasks/find-tasks";
    }


    // ================= DEFAULT VIEW METHODS =================

    /**
     * Отображает страницу со списком всех задач.
     *
     * @param model Модель Spring UI.
     * @param currentUser Текущий пользователь.
     * @return Имя Thymeleaf-шаблона для всех задач.
     */
    @GetMapping
    @Loggable
    public String getAllTasks(Model model, @AuthenticationPrincipal User currentUser) {
        List<Task> userTasks = taskService.getUserTasks(currentUser);
        model.addAttribute("tasks", taskService.findAllTasks());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("countOfMyTasksMessage", taskAssignFacade.getCountOfMyTasksMessage(userTasks));
        log.info("Current User: {}", currentUser);
        return "tasks/all-tasks";
    }

    /**
     * Отображает страницу со списком задач, назначенных текущему пользователю.
     *
     * @param currentUser Текущий пользователь.
     * @param model Модель Spring UI.
     * @return Имя Thymeleaf-шаблона для "Моих задач".
     */
    @GetMapping("/my-tasks")
    @Loggable
    public String getMyTasks(@AuthenticationPrincipal User currentUser, Model model) {
        log.info("Current user roles - " + currentUser.getRoles().toString());
        List<Task> userTasks = taskService.getUserTasks(currentUser);
        model.addAttribute("tasks", userTasks);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("countOfMyTasksMessage", taskAssignFacade.getCountOfMyTasksMessage(userTasks));
        model.addAttribute("users", userService.getAllUsers());
        return "tasks/my-tasks";
    }


    // ================= CREATE METHOD =================

    /**
     * Обрабатывает POST-запрос на создание новой задачи.
     *
     * @param taskRequest DTO с данными новой задачи.
     * @param currentUser Пользователь, создающий задачу.
     * @return Перенаправление на "/tasks" или на "/tasks/my-tasks".
     */
    @PostMapping()
    @Loggable
    public String saveTask(CreateTaskRequest taskRequest, @AuthenticationPrincipal User currentUser) {

        log.info("Creating task - " + taskRequest.getTitle());

        try{
            taskService.createTask(taskRequest, currentUser);
        }catch (NoRightsException e){
            log.info("User don't have permission to create task");
            return REDIRECT_ALL_TASKS;
        }catch (Exception e){
            log.info("Exception while creating task - " + taskRequest.getTitle());
            return REDIRECT_ALL_TASKS;
        }
        return REDIRECT_ALL_TASKS;
    }
}