package com.rwms.project.service;

import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.project.dto.AddContributorsRequest;
import com.rwms.project.dto.CreateProjectRequest;
import com.rwms.project.dto.ProjectResponse;
import com.rwms.project.dto.TeamLeaderRequestResponse;
import com.rwms.project.entity.Project;
import com.rwms.project.entity.TeamLeaderRequest;
import com.rwms.project.repository.ProjectRepository;
import com.rwms.project.repository.TeamLeaderRequestRepository;
import com.rwms.task.entity.Task;
import com.rwms.task.repository.TaskRepository;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.rwms.notification.dto.NotificationEvent;
import com.rwms.notification.entity.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamLeaderRequestRepository teamLeaderRequestRepository;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, TeamLeaderRequestRepository teamLeaderRequestRepository, TaskRepository taskRepository, ApplicationEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.teamLeaderRequestRepository = teamLeaderRequestRepository;
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    private ProjectResponse toResponse(Project project) {
        int taskCount = (int) taskRepository.countByProjectId(project.getId());
        int completedTaskCount = (int) taskRepository.countByProjectIdAndStatus(project.getId(), Task.TaskStatus.APPROVED);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .department(project.getDepartment())
                .description(project.getDescription())
                .teamLeaderName(project.getTeamLeader() != null ? project.getTeamLeader().getFullName() : null)
                .contributorCount(project.getContributors().size())
                .taskCount(taskCount) 
                .completedTaskCount(completedTaskCount)
                .build();
    }

    private TeamLeaderRequestResponse toTlResponse(TeamLeaderRequest request) {
        return TeamLeaderRequestResponse.builder()
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getFullName())
                .requesterEmail(request.getRequester().getEmail())
                .projectId(request.getProject().getId())
                .projectName(request.getProject().getName())
                .status(request.getStatus().name())
                .submittedAt(request.getSubmittedAt())
                .build();
    }

    @Override
    public ProjectResponse createProject(CreateProjectRequest request, String creatorEmail) {
        Project project = Project.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .description(request.getDescription())
                .build();
        return toResponse(projectRepository.save(project));
    }

    @Override
    public List<ProjectResponse> getProjectsByDepartment(String department) {
        return projectRepository.findByDepartment(department)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getMyProjects(String adminEmail) {
        User tl = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectRepository.findByTeamLeaderId(tl.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return toResponse(project);
    }

    @Override
    public void addContributors(Long projectId, AddContributorsRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        for (Long userId : request.getUserIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            if (!project.getContributors().contains(user)) {
                project.getContributors().add(user);
            }
        }
        projectRepository.save(project);
    }

    @Override
    public void removeContributor(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        project.getContributors().remove(user);
        projectRepository.save(project);
    }

    @Override
    public void submitTeamLeaderRequest(Long projectId, String adminEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User requester = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminEmail));

        TeamLeaderRequest request = TeamLeaderRequest.builder()
                .project(project)
                .requester(requester)
                .status(TeamLeaderRequest.RequestStatus.PENDING)
                .build();
        teamLeaderRequestRepository.save(request);

        List<User> managers = userRepository.findByRole(User.Role.MANAGER);
        for (User manager : managers) {
            eventPublisher.publishEvent(NotificationEvent.builder()
                    .recipient(manager)
                    .title("New Team Leader Request")
                    .message(requester.getFullName() + " has requested to be Team Leader for project: " + project.getName())
                    .type(NotificationType.TL_ASSIGNMENT_REQUEST)
                    .build());
        }
    }

    @Override
    public List<TeamLeaderRequestResponse> getPendingTeamLeaderRequests() {
        return teamLeaderRequestRepository.findByStatus(TeamLeaderRequest.RequestStatus.PENDING)
                .stream()
                .map(this::toTlResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void approveTeamLeaderRequest(Long requestId) {
        TeamLeaderRequest request = teamLeaderRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
        
        if (request.getStatus() != TeamLeaderRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not pending");
        }

        request.setStatus(TeamLeaderRequest.RequestStatus.APPROVED);
        teamLeaderRequestRepository.save(request);

        Project project = request.getProject();
        project.setTeamLeader(request.getRequester());
        projectRepository.save(project);
    }

    @Override
    public void rejectTeamLeaderRequest(Long requestId) {
        TeamLeaderRequest request = teamLeaderRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
        
        if (request.getStatus() != TeamLeaderRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not pending");
        }

        request.setStatus(TeamLeaderRequest.RequestStatus.REJECTED);
        teamLeaderRequestRepository.save(request);
    }
}
