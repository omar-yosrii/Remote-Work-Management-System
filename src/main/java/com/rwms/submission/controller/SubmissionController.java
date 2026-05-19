package com.rwms.submission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwms.submission.dto.*;
import com.rwms.submission.service.SubmissionService;
import com.rwms.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping(value = "/task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResponse> submitTask(@PathVariable Long taskId,
                                                         @RequestParam("request") String requestJson,
                                                         @RequestParam(value = "file", required = false) MultipartFile file,
                                                         @AuthenticationPrincipal User user) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper();
        SubmitTaskRequest request = mapper.readValue(requestJson, SubmitTaskRequest.class);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.submitTask(taskId, request, file, user.getEmail()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getMySubmissions(user.getEmail()));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<SubmissionDetailResponse> getSubmissionDetail(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmissionDetail(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<SubmissionResponse> reviewSubmission(@PathVariable Long id,
                                                               @Valid @RequestBody ReviewRequest request,
                                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.reviewSubmission(id, request, user.getEmail()));
    }

    @GetMapping("/pending/project/{projectId}")
    public ResponseEntity<List<SubmissionResponse>> getPendingSubmissionsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(submissionService.getPendingSubmissionsByProject(projectId));
    }

    // --- Comments ---

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id,
                                                      @Valid @RequestBody CommentRequest request,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.addComment(id, request, user.getEmail()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getComments(id, user.getEmail()));
    }
}
