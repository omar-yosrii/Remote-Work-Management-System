package com.rwms.user.controller;

import com.rwms.project.dto.TeamLeaderRequestResponse;
import com.rwms.project.service.IProjectService;
import com.rwms.user.dto.UserResponse;
import com.rwms.user.service.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    private final IUserService userService;
    private final IProjectService projectService;

    public ManagerController(IUserService userService, IProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/pending-admins")
    public ResponseEntity<List<UserResponse>> getPendingAdmins() {
        return ResponseEntity.ok(userService.getPendingAdmins());
    }

    @PostMapping("/approve-admin/{userId}")
    public ResponseEntity<Void> approveAdmin(@PathVariable Long userId) {
        userService.approveAdmin(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject-admin/{userId}")
    public ResponseEntity<Void> rejectAdmin(@PathVariable Long userId) {
        userService.rejectAdmin(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending-tl-requests")
    public ResponseEntity<List<TeamLeaderRequestResponse>> getPendingTeamLeaderRequests() {
        return ResponseEntity.ok(projectService.getPendingTeamLeaderRequests());
    }

    @PostMapping("/approve-tl/{requestId}")
    public ResponseEntity<Void> approveTeamLeaderRequest(@PathVariable Long requestId) {
        projectService.approveTeamLeaderRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject-tl/{requestId}")
    public ResponseEntity<Void> rejectTeamLeaderRequest(@PathVariable Long requestId) {
        projectService.rejectTeamLeaderRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRoleAndDepartment(@PathVariable Long userId,
                                                                    @RequestParam(required = false) String role,
                                                                    @RequestParam(required = false) String department) {
        return ResponseEntity.ok(userService.updateUserRoleAndDepartment(userId, role, department));
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
