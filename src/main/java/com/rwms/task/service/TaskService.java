package com.rwms.task.service;

import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.project.entity.Project;
import com.rwms.project.repository.ProjectRepository;
import com.rwms.task.dto.*;
import com.rwms.task.entity.Subtask;
import com.rwms.task.entity.Task;
import com.rwms.task.repository.SubtaskRepository;
import com.rwms.task.repository.TaskRepository;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.rwms.notification.dto.NotificationEvent;
import com.rwms.notification.entity.NotificationType;
import com.rwms.audit.service.AuditLogService;
import com.rwms.audit.command.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService implements ITaskService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    public TaskService(TaskRepository taskRepository, SubtaskRepository subtaskRepository,
                       ProjectRepository projectRepository, UserRepository userRepository,
                       ApplicationEventPublisher eventPublisher, AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.auditLogService = auditLogService;
    }

    private SubtaskResponse toSubtaskResponse(Subtask subtask) {
        return SubtaskResponse.builder()
                .id(subtask.getId())
                .name(subtask.getName())
                .completedByEmployee(subtask.isCompletedByEmployee())
                .approvedByAdmin(subtask.isApprovedByAdmin())
                .completedAt(subtask.getCompletedAt())
                .employeeComment(subtask.getEmployeeComment())
                .build();
    }

    private TaskResponse toTaskResponse(Task task) {
        List<SubtaskResponse> subtasks = subtaskRepository.findByTaskId(task.getId())
                .stream()
                .map(this::toSubtaskResponse)
                .collect(Collectors.toList());

        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .githubRepoLink(task.getGithubRepoLink())
                .status(task.getStatus().name())
                .assignedEmployeeName(task.getAssignedEmployee() != null ? task.getAssignedEmployee().getFullName() : null)
                .projectName(task.getProject().getName())
                .subtasks(subtasks)
                .build();
    }

    @Override
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, String adminEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        Task task = Task.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .githubRepoLink(request.getGithubRepoLink())
                .project(project)
                .status(Task.TaskStatus.PENDING)
                .build();

        task = taskRepository.save(task);

        if (request.getSubtasks() != null) {
            for (CreateSubtaskRequest subReq : request.getSubtasks()) {
                Subtask subtask = Subtask.builder()
                        .name(subReq.getName())
                        .description(subReq.getDescription())
                        .task(task)
                        .build();
                subtaskRepository.save(subtask);
            }
        }

        return toTaskResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (request.getName() != null) task.setName(request.getName());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());
        if (request.getGithubRepoLink() != null) task.setGithubRepoLink(request.getGithubRepoLink());

        return toTaskResponse(taskRepository.save(task));
    }

    @Override
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    @Override
    public SubtaskResponse addSubtask(Long taskId, CreateSubtaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        Subtask subtask = Subtask.builder()
                .name(request.getName())
                .description(request.getDescription())
                .task(task)
                .build();

        return toSubtaskResponse(subtaskRepository.save(subtask));
    }

    @Override
    public void updateSubtask(Long subtaskId, CreateSubtaskRequest request) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found: " + subtaskId));

        if (request.getName() != null) subtask.setName(request.getName());
        if (request.getDescription() != null) subtask.setDescription(request.getDescription());

        subtaskRepository.save(subtask);
    }

    @Override
    public void deleteSubtask(Long subtaskId) {
        if (!subtaskRepository.existsById(subtaskId)) {
            throw new ResourceNotFoundException("Subtask not found: " + subtaskId);
        }
        subtaskRepository.deleteById(subtaskId);
    }

    @Override
    public void assignTask(Long taskId, AssignTaskRequest request, String performedByEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmployeeId()));

        Project project = projectRepository.findById(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getContributors().contains(employee)) {
            throw new IllegalArgumentException("Employee is not a contributor to this project");
        }

        task.setAssignedEmployee(employee);
        taskRepository.save(task);

        eventPublisher.publishEvent(NotificationEvent.builder()
                .recipient(employee)
                .title("New Task Assigned")
                .message("You have been assigned to task: " + task.getName())
                .type(NotificationType.TASK_ASSIGNED)
                .build());

        User performedBy = userRepository.findByEmail(performedByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        auditLogService.log(new TaskAssignedCommand(performedBy, performedByEmail, "Assigned task '" + task.getName() + "' to " + employee.getEmail()));
    }

    @Override
    public List<TaskResponse> getMyTasks(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return taskRepository.findByAssignedEmployeeId(employee.getId())
                .stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse startTask(Long taskId, String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (task.getAssignedEmployee() == null || !task.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Task is not assigned to you");
        }

        List<Task> myTasks = taskRepository.findByAssignedEmployeeId(employee.getId());
        boolean hasInProgress = myTasks.stream()
                .anyMatch(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS && !t.getId().equals(taskId));

        if (hasInProgress) {
            throw new IllegalStateException("You already have a task IN_PROGRESS");
        }

        task.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        return toTaskResponse(taskRepository.save(task));
    }

    @Override
    public void markSubtaskComplete(Long subtaskId, String employeeEmail, String comment) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found: " + subtaskId));

        Task task = subtask.getTask();
        if (task.getAssignedEmployee() == null || !task.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Task is not assigned to you");
        }

        if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task must be IN_PROGRESS to mark subtasks complete");
        }

        subtask.setCompletedByEmployee(true);
        subtask.setCompletedAt(LocalDateTime.now());
        subtask.setEmployeeComment(comment);
        subtaskRepository.save(subtask);

        auditLogService.log(new SubtaskCompletedCommand(employee, employeeEmail, "Completed subtask '" + subtask.getName() + "'"));
    }
}