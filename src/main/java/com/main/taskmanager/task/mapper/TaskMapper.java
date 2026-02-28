package com.main.taskmanager.task.mapper;

import com.main.taskmanager.dto.TaskDTO;
import com.main.taskmanager.web.model.CreateTaskRequest;
import com.main.taskmanager.task.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Интерфейс-маппер, использующий библиотеку MapStruct для преобразования данных
 * между сущностью {@link Task} (для работы с БД),
 * объектом запроса {@link CreateTaskRequest},
 * и объектом передачи данных {@link TaskDTO}.
 *
 * <p>Аннотация {@code @Mapper(componentModel = "spring")} указывает, что MapStruct
 * должен генерировать реализацию маппера как Spring Component.</p>
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Преобразует сущность {@link Task} в объект передачи данных {@link TaskDTO}.
     *
     * <p>Выполняются следующие преобразования и маппинги:</p>
     * <ul>
     * <li>ID назначенного пользователя (Assignee) и его имя (FullName) извлекаются из связанной сущности {@code assignee}.</li>
     * <li>Поля времени ({@code createdAt}, {@code updatedAt}) форматируются в заданный строковый формат.</li>
     * </ul>
     *
     * @param task Исходная сущность задачи.
     * @return Объект {@link TaskDTO} для передачи в контроллер или представление.
     */
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.fullName", target = "assigneeName")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "dd.MM.yyyy HH:mm")
    TaskDTO toDTO(Task task);

    /**
     * Преобразует объект запроса на создание задачи {@link CreateTaskRequest} в сущность {@link Task}.
     * <p>
     * <b>Важные правила маппинга:</b>
     * <ul>
     * <li>{@code id} и {@code assignee} игнорируются, так как задаются отдельно в сервисе.</li>
     * <li>{@code status} устанавливается константой {@code OPEN} при создании.</li>
     * <li>{@code createdAt} и {@code updatedAt} инициализируются текущим временем с помощью Java Expression.</li>
     * </ul>
     *
     * @param request Объект запроса от клиента.
     * @return Новая сущность {@link Task} с инициализированными системными полями.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Task toEntity(CreateTaskRequest request);
}