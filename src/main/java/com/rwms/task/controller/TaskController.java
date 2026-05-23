package com.rwms.task.controller;

import com.rwms.task.dto.*;
import com.rwms.task.service.ITaskService;
import com.rwms.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final ITaskService taskService;

    public TaskController(ITaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId,
                                                   @Valid @RequestBody CreateTaskRequest request,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request, user.getEmail()));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long taskId,
                                                   @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<SubtaskResponse> addSubtask(@PathVariable Long taskId,
                                                      @Valid @RequestBody CreateSubtaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addSubtask(taskId, request));
    }

    @PutMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<Void> updateSubtask(@PathVariable Long taskId,
                                              @PathVariable Long subtaskId,
                                              @Valid @RequestBody CreateSubtaskRequest request) {
        taskService.updateSubtask(subtaskId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(@PathVariable Long taskId,
                                              @PathVariable Long subtaskId) {
        taskService.deleteSubtask(subtaskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<Void> assignTask(@PathVariable Long taskId,
                                           @Valid @RequestBody AssignTaskRequest request,
                                           @AuthenticationPrincipal User user) {
        taskService.assignTask(taskId, request, user.getEmail());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getMyTasks(user.getEmail()));
    }

    @PostMapping("/{taskId}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable Long taskId,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.startTask(taskId, user.getEmail()));
    }

    @PostMapping("/{taskId}/subtasks/{subtaskId}/complete")
    public ResponseEntity<Void> markSubtaskComplete(@PathVariable Long taskId,
                                                    @PathVariable Long subtaskId,
                                                    @RequestBody(required = false) String comment,
                                                    @AuthenticationPrincipal User user) {
        taskService.markSubtaskComplete(subtaskId, user.getEmail(), comment);
        return ResponseEntity.ok().build();
    }
}
