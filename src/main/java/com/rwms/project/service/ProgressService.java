package com.rwms.project.service;

import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.project.dto.ProjectProgressResponse;
import com.rwms.project.dto.TaskProgressItem;
import com.rwms.project.entity.Project;
import com.rwms.project.repository.ProjectRepository;
import com.rwms.task.entity.Task;
import com.rwms.task.repository.SubtaskRepository;
import com.rwms.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

    public ProgressService(ProjectRepository projectRepository, TaskRepository taskRepository, SubtaskRepository subtaskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
    }

    public ProjectProgressResponse getProjectProgress(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.APPROVED)
                .count();

        long totalSubtasks = 0;
        long approvedSubtasks = 0;

        List<TaskProgressItem> taskProgressItems = tasks.stream().map(task -> {
            long taskSubtasks = subtaskRepository.countByTaskId(task.getId());
            long taskApprovedSubtasks = subtaskRepository.countByTaskIdAndApprovedByAdminTrue(task.getId());
            
            return TaskProgressItem.builder()
                    .taskId(task.getId())
                    .taskName(task.getName())
                    .status(task.getStatus().name())
                    .totalSubtasks(taskSubtasks)
                    .completedSubtasks(taskApprovedSubtasks)
                    .build();
        }).collect(Collectors.toList());

        for (TaskProgressItem item : taskProgressItems) {
            totalSubtasks += item.getTotalSubtasks();
            approvedSubtasks += item.getCompletedSubtasks();
        }

        return ProjectProgressResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .totalSubtasks(totalSubtasks)
                .approvedSubtasks(approvedSubtasks)
                .tasks(taskProgressItems)
                .build();
    }

    public List<ProjectProgressResponse> getAllProjectsSummary() {
        return projectRepository.findAll().stream()
                .map(project -> getProjectProgress(project.getId()))
                .collect(Collectors.toList());
    }
}
