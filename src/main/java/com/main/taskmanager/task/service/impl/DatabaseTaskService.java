package com.main.taskmanager.task.service.impl;

import com.main.taskmanager.exception.NoRightsException;
import com.main.taskmanager.exception.NotFoundTaskException;
import com.main.taskmanager.exception.NotFoundUserException;
import com.main.taskmanager.task.mapper.TaskMapper;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.task.service.TaskService;
import com.main.taskmanager.user.model.enumclasses.Role;
import com.main.taskmanager.user.repository.UserRepository;
import com.main.taskmanager.user.service.UserService;
import com.main.taskmanager.web.model.CreateTaskRequest;
import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.task.model.enumclasses.Priority;

import com.main.taskmanager.task.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Реализация интерфейса {@link TaskService}, использующая Spring Data JPA для взаимодействия с базой данных.
 * Отвечает за выполнение бизнес-логики, включая создание, назначение, удаление задач,
 * а также за проверку ролей пользователя перед выполнением критических операций.
 */
@Service
@Transactional // Обеспечивает транзакционность всех методов сервиса
@RequiredArgsConstructor
public class DatabaseTaskService implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;

    /**
     * @inheritDoc
     */
    @Override
    public List<Task> findAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Task> getUserTasks(User user) {
        return taskRepository.findByAssigneeOrderByCreatedAtDesc(user);
    }

    /**
     * @inheritDoc
     * @throws NotFoundTaskException Если задача с указанным ID не найдена.
     */
    @Override
    public Optional<Task> getTaskById(Long id) {
        if (taskRepository.existsById(id)) {
            return taskRepository.findById(id);
        }
        throw new NotFoundTaskException("Task not found");
    }

    /**
     * @inheritDoc
     * <p>
     * <b>Требует роли: {@link Role#ADMINISTRATOR}</b>.
     * </p>
     * @throws NoRightsException Если текущий пользователь не является Администратором.
     */
    @Override
    @Transactional
    public Task createTask(CreateTaskRequest request, User user) {
        if(!user.getRole().equals(Role.ADMINISTRATOR)) {
            throw new NoRightsException("You are not allowed to perform this action");
        }
        // Преобразование DTO в сущность и сохранение
        Task task = taskMapper.toEntity(request);
        return taskRepository.save(task);
    }

    /**
     * @inheritDoc
     * <p>
     * <b>Требует роли: {@link Role#ADMINISTRATOR}</b>.
     * </p>
     * @throws NoRightsException Если текущий пользователь не является Администратором.
     * @throws NotFoundTaskException Если задача не найдена.
     */
    @Override
    @Transactional
    public void deleteTaskById(Long taskId, User user) {
        if(!user.getRole().equals(Role.ADMINISTRATOR)) {
            throw new NoRightsException("You are not allowed to perform this action");
        }
        if(!taskRepository.existsById(taskId)) {
            throw new NotFoundTaskException("Task not found");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id " + taskId));
        taskRepository.delete(task);
    }

    /**
     * @inheritDoc
     * <p>
     * <b>Проверка прав:</b> Разрешено {@link Role#ADMINISTRATOR} или текущему назначенному пользователю (только для изменения статуса).
     * </p>
     * @throws NotFoundTaskException Если задача не найдена.
     * @throws NotFoundUserException Если пользователь, которому назначается задача, не найден.
     * @throws NoRightsException Если пользователь не является Администратором и пытается изменить задачу, ему не назначенную.
     */
    @Override
    @Transactional
    public Task assignTask(Long taskId, Long assigneeId, Priority priority, TaskStatus taskStatus,  User user) {
        // Проверка существования задачи и назначенного пользователя
        if(!taskRepository.existsById(taskId)) {
            throw new NotFoundTaskException("Task not found");
        }
        if(!userRepository.existsById(assigneeId)) {
            throw new NotFoundUserException("User not found");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id " + taskId));
        User assignee = userService.getUserById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id " + assigneeId));

        // Проверка прав: Только Администратор или текущий назначенец может менять задачу
        if(!user.getRole().equals(Role.ADMINISTRATOR) && (task.getAssignee() == null || !user.getId().equals(task.getAssignee().getId()))) {
            throw new NoRightsException("You are not allowed to perform this action");
        }

        task.setAssignee(assignee);
        task.setPriority(priority);
        task.setStatus(taskStatus);
        return taskRepository.save(task);
    }

    /**
     * @inheritDoc
     * <p>
     * <b>Проверка прав:</b> Разрешено {@link Role#ADMINISTRATOR} или текущему назначенному пользователю.
     * </p>
     * @throws NotFoundTaskException Если задача не найдена.
     * @throws NoRightsException Если пользователь не является Администратором и пытается снять назначение с чужой задачи.
     */
    @Override
    @Transactional
    public Task unassignTask(Long taskId, User user) {
        if(!taskRepository.existsById(taskId)) {
            throw new NotFoundTaskException("Task not found");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id " + taskId));

        // Проверка прав: Только Администратор или текущий назначенец может снять назначение
        if(!user.getRole().equals(Role.ADMINISTRATOR) && (task.getAssignee() == null || !user.getId().equals(task.getAssignee().getId()))) {
            throw new NoRightsException("You are not allowed to perform this action");
        }

        task.setAssignee(null);
        return taskRepository.save(task);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean existsById(Long id) {
        return taskRepository.existsById(id);
    }
}