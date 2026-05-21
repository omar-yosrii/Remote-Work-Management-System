package com.rwms.timer.entity;

import com.rwms.task.entity.Task;
import com.rwms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSession {

    public enum SessionState {
        RUNNING, ON_BREAK, COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionState state = SessionState.RUNNING;

    @Builder.Default
    private long workedSeconds = 0;

    @Builder.Default
    private long breakSeconds = 0;

    @Builder.Default
    private boolean breakTaken = false;

    private LocalDateTime sessionStartedAt;

    private LocalDateTime breakStartedAt;

    private LocalDateTime lastSyncedAt;

    @Builder.Default
    private boolean submissionPageTriggered = false;

    @Builder.Default
    private boolean breakWarningSent = false;

    @Builder.Default
    private boolean breakEndingWarningSent = false;
}
