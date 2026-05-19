package com.rwms.project.controller;

import com.rwms.project.dto.ProjectProgressResponse;
import com.rwms.project.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectProgressResponse> getProjectProgress(@PathVariable Long projectId) {
        return ResponseEntity.ok(progressService.getProjectProgress(projectId));
    }

    @GetMapping("/manager/all")
    public ResponseEntity<List<ProjectProgressResponse>> getAllProjectsSummary() {
        return ResponseEntity.ok(progressService.getAllProjectsSummary());
    }
}
