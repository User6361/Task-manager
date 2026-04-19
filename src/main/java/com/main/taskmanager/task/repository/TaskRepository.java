package com.main.taskmanager.task.repository;

import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий для доступа к данным сущности {@link Task}.
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции.
 * Содержит кастомные методы (Derived Query Methods) для получения списков задач
 * с сортировкой по дате создания.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Находит все задачи в системе, отсортированные по дате создания в обратном порядке (от новых к старым).
     * <p>
     * Эквивалентно JPQL: {@code SELECT t FROM Task t ORDER BY t.createdAt DESC}.
     * </p>
     * @return Список всех задач.
     */
    List<Task> findAllByOrderByCreatedAtDesc();

    /**
     * Находит все задачи, назначенные конкретному пользователю (Assignee).
     * Результаты сортируются по дате создания в обратном порядке.
     *
     * @param user Сущность {@link User}, которой назначены задачи.
     * @return Список задач, назначенных указанному пользователю.
     */
    List<Task> findByAssigneeOrderByCreatedAtDesc(User user);

}