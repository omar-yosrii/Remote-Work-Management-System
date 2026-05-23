package com.rwms.task.service;

import com.rwms.task.dto.*;

import java.util.List;

public interface ITaskService {

    TaskResponse createTask(Long projectId, CreateTaskRequest request, String adminEmail);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request);

    void deleteTask(Long taskId);

    SubtaskResponse addSubtask(Long taskId, CreateSubtaskRequest request);

    void updateSubtask(Long subtaskId, CreateSubtaskRequest request);

    void deleteSubtask(Long subtaskId);

    void assignTask(Long taskId, AssignTaskRequest request, String performedByEmail);

    List<TaskResponse> getMyTasks(String employeeEmail);

    TaskResponse startTask(Long taskId, String employeeEmail);

    void markSubtaskComplete(Long subtaskId, String employeeEmail, String comment);
}
