package com.rwms.submission.entity;

import com.rwms.task.entity.Task;
import com.rwms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmission {

    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String accomplishmentComment;

    private String attachmentPath;

    private String alternativeGithubLink;


    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}
