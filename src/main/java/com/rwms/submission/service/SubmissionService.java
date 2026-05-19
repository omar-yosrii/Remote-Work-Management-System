package com.rwms.submission.service;

import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.submission.dto.*;
import com.rwms.submission.entity.SubmissionComment;
import com.rwms.submission.entity.TaskSubmission;
import com.rwms.submission.repository.SubmissionCommentRepository;
import com.rwms.submission.repository.TaskSubmissionRepository;
import com.rwms.task.dto.SubtaskResponse;
import com.rwms.task.entity.Subtask;
import com.rwms.task.entity.Task;
import com.rwms.task.repository.SubtaskRepository;
import com.rwms.task.repository.TaskRepository;
import com.rwms.timer.service.TimerService;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final SubmissionCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final UserRepository userRepository;
    private final TimerService timerService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public SubmissionService(TaskSubmissionRepository submissionRepository, SubmissionCommentRepository commentRepository,
                             TaskRepository taskRepository, SubtaskRepository subtaskRepository,
                             UserRepository userRepository, TimerService timerService) {
        this.submissionRepository = submissionRepository;
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
        this.userRepository = userRepository;
        this.timerService = timerService;
    }

    private SubmissionResponse toResponse(TaskSubmission sub) {
        return SubmissionResponse.builder()
                .id(sub.getId())
                .taskName(sub.getTask().getName())
                .submittedAt(sub.getSubmittedAt())
                .reviewStatus(sub.getReviewStatus().name())
                .rejectionReason(sub.getRejectionReason())
                .build();
    }

    private CommentResponse toCommentResponse(SubmissionComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getFullName())
                .content(comment.getContent())
                .isPrivateNote(comment.isPrivateNote())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public SubmissionResponse submitTask(Long taskId, SubmitTaskRequest request, MultipartFile file, String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getAssignedEmployee() == null || !task.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Task not assigned to you");
        }
        if (task.getStatus() != Task.TaskStatus.IN_PROGRESS && task.getStatus() != Task.TaskStatus.REJECTED) {
            throw new IllegalStateException("Task must be IN_PROGRESS or REJECTED to submit");
        }

        String filePath = null;
        if (file != null && !file.isEmpty()) {
            try {
                String folderPath = uploadDir + File.separator + taskId;
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(folderPath + File.separator + fileName);
                Files.write(path, file.getBytes());
                filePath = path.toString();
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }

        TaskSubmission submission = submissionRepository.findByTaskId(taskId).orElse(new TaskSubmission());
        submission.setTask(task);
        submission.setEmployee(employee);
        submission.setAccomplishmentComment(request.getAccomplishmentComment());
        if (filePath != null) submission.setAttachmentPath(filePath);
        if (request.getAlternativeGithubLink() != null) submission.setAlternativeGithubLink(request.getAlternativeGithubLink());
        submission.setReviewStatus(TaskSubmission.ReviewStatus.PENDING);

        submission = submissionRepository.save(submission);

        task.setStatus(Task.TaskStatus.SUBMITTED);
        taskRepository.save(task);

        try {
            timerService.endSession(employeeEmail);
        } catch (Exception e) {
            // Ignore if no active session
        }

        return toResponse(submission);
    }

    public SubmissionResponse reviewSubmission(Long submissionId, ReviewRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        Task task = submission.getTask();
        if (task.getProject().getTeamLeader() == null || !task.getProject().getTeamLeader().getId().equals(admin.getId())) {
            throw new IllegalArgumentException("Only the project Team Leader can review submissions");
        }

        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            submission.setReviewStatus(TaskSubmission.ReviewStatus.APPROVED);
            task.setStatus(Task.TaskStatus.APPROVED);
            
            // Mark subtasks as approved
            List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
            for (Subtask st : subtasks) {
                if (st.isCompletedByEmployee()) {
                    st.setApprovedByAdmin(true);
                }
            }
            subtaskRepository.saveAll(subtasks);
            
        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            submission.setReviewStatus(TaskSubmission.ReviewStatus.REJECTED);
            submission.setRejectionReason(request.getRejectionReason());
            task.setStatus(Task.TaskStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Invalid action. Use APPROVE or REJECT");
        }

        taskRepository.save(task);
        return toResponse(submissionRepository.save(submission));
    }

    public SubmissionDetailResponse getSubmissionDetail(Long submissionId) {
        TaskSubmission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        List<SubtaskResponse> subtasks = subtaskRepository.findByTaskId(sub.getTask().getId())
                .stream()
                .map(st -> SubtaskResponse.builder()
                        .id(st.getId())
                        .name(st.getName())
                        .completedByEmployee(st.isCompletedByEmployee())
                        .approvedByAdmin(st.isApprovedByAdmin())
                        .completedAt(st.getCompletedAt())
                        .employeeComment(st.getEmployeeComment())
                        .build())
                .collect(Collectors.toList());

        return SubmissionDetailResponse.builder()
                .submissionInfo(toResponse(sub))
                .subtasks(subtasks)
                .accomplishmentComment(sub.getAccomplishmentComment())
                .attachmentPath(sub.getAttachmentPath())
                .alternativeGithubLink(sub.getAlternativeGithubLink())
                .build();
    }

    public List<SubmissionResponse> getMySubmissions(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return submissionRepository.findByEmployeeId(employee.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SubmissionResponse> getPendingSubmissionsByProject(Long projectId) {
        return submissionRepository.findPendingByProjectId(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // --- Comments & Notes ---

    public CommentResponse addComment(Long submissionId, CommentRequest request, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // If private note, only allow admin/manager
        if (request.isPrivateNote() && author.getRole() == User.Role.EMPLOYEE) {
            throw new IllegalArgumentException("Employees cannot add private notes");
        }

        SubmissionComment comment = SubmissionComment.builder()
                .submission(submission)
                .author(author)
                .content(request.getContent())
                .isPrivateNote(request.isPrivateNote())
                .build();

        return toCommentResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> getComments(Long submissionId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole() == User.Role.EMPLOYEE) {
            return commentRepository.findBySubmissionIdAndIsPrivateNoteFalseOrderByCreatedAtAsc(submissionId)
                    .stream().map(this::toCommentResponse).collect(Collectors.toList());
        } else {
            return commentRepository.findBySubmissionIdOrderByCreatedAtAsc(submissionId)
                    .stream().map(this::toCommentResponse).collect(Collectors.toList());
        }
    }
}
