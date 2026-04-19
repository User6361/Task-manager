package com.main.taskmanager.task.mapper;

import com.main.taskmanager.task.model.Task;
import com.main.taskmanager.web.model.CreateTaskRequest;
import com.main.taskmanager.web.model.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.fullName", target = "assigneeName")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "dd.MM.yyyy HH:mm")
    TaskResponse toDTO(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Task toEntity(CreateTaskRequest request);
}