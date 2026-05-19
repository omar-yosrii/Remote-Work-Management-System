package com.rwms.submission.entity;

import com.rwms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submission_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private TaskSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    private boolean isPrivateNote = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
