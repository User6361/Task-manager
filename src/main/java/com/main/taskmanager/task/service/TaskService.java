package com.main.taskmanager.task.service;

import com.main.taskmanager.task.model.Task;
import java.util.List;
import java.util.Optional;
import com.main.taskmanager.task.model.enumclasses.Priority;
import com.main.taskmanager.task.model.enumclasses.TaskStatus;
import com.main.taskmanager.user.model.User;
import com.main.taskmanager.web.model.CreateTaskRequest;

/**
 * Интерфейс сервисного слоя для управления сущностью {@link Task}.
 * Определяет бизнес-логику для всех операций с задачами, включая создание,
 * назначение, изменение статуса и удаление, а также отвечает за проверку
 * прав доступа (авторизацию) внутри бизнес-логики.
 */
public interface TaskService {

    /**
     * Возвращает список всех задач в системе, отсортированных по дате создания.
     * @return Список всех задач.
     */
    List<Task> findAllTasks();

    /**
     * Возвращает список задач, назначенных конкретному пользователю.
     * @param user Пользователь, для которого запрашиваются задачи.
     * @return Список задач, назначенных пользователю.
     */
    List<Task> getUserTasks(User user);

    /**
     * Находит задачу по её уникальному идентификатору.
     * @param id ID задачи.
     * @return {@link Optional} с найденной задачей или пустым значением.
     */
    Optional<Task> getTaskById(Long id);

    /**
     * Создает новую задачу на основе объекта запроса.
     * Автоматически устанавливает статус {@code OPEN} и дату создания.
     * @param request Объект {@link CreateTaskRequest} с данными новой задачи.
     * @param user Пользователь, создающий задачу (для логирования и проверки прав).
     * @return Созданная и сохраненная сущность {@link Task}.
     */
    Task createTask(CreateTaskRequest request, User user);

    /**
     * Обновляет существующую задачу, изменяя её назначенного пользователя, приоритет и статус.
     * Метод также используется для изменения статуса уже назначенными пользователями.
     *
     * @param taskId ID изменяемой задачи.
     * @param assigneeId ID пользователя, которому назначается задача.
     * @param priority Новый приоритет задачи.
     * @param taskStatus Новый статус задачи.
     * @param user Пользователь, выполняющий операцию (должен быть {@code ADMINISTRATOR} или назначенным пользователем).
     * @return Обновленная сущность {@link Task}.
     */
    Task assignTask(Long taskId, Long assigneeId, Priority priority, TaskStatus taskStatus, User user);

    /**
     * Удаляет задачу по её ID.
     * Доступно только для {@code ADMINISTRATOR}.
     *
     * @param id ID задачи, которую нужно удалить.
     * @param user Пользователь, выполняющий удаление.
     */
    void deleteTaskById(Long id, User user);

    /**
     * Снимает назначенного пользователя с задачи (устанавливает assignee = null).
     * Доступно только для {@code ADMINISTRATOR}.
     *
     * @param taskId ID задачи, с которой снимается назначение.
     * @param user Пользователь, выполняющий операцию.
     * @return Обновленная сущность {@link Task}.
     */
    Task unassignTask(Long taskId, User user);

    /**
     * Проверяет существование задачи с заданным ID.
     * @param id ID задачи.
     * @return {@code true}, если задача существует, иначе {@code false}.
     */
    boolean existsById(Long id);
}