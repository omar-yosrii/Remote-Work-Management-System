package com.rwms.project.controller;

import com.rwms.project.dto.AddContributorsRequest;
import com.rwms.project.dto.CreateProjectRequest;
import com.rwms.project.dto.ProjectResponse;
import com.rwms.project.service.IProjectService;
import com.rwms.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final IProjectService projectService;

    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, user.getEmail()));
    }

    @GetMapping("/department/{dept}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByDepartment(@PathVariable String dept) {
        return ResponseEntity.ok(projectService.getProjectsByDepartment(dept));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(projectService.getMyProjects(user.getEmail()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping("/{id}/contributors")
    public ResponseEntity<Void> addContributors(@PathVariable Long id,
                                                @Valid @RequestBody AddContributorsRequest request) {
        projectService.addContributors(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/contributors/{userId}")
    public ResponseEntity<Void> removeContributor(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeContributor(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/request-tl")
    public ResponseEntity<Void> requestTeamLeader(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        projectService.submitTeamLeaderRequest(id, user.getEmail());
        return ResponseEntity.ok().build();
    }
}
