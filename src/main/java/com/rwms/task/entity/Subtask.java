package com.rwms.task.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subtasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Builder.Default
    private boolean completedByEmployee = false;

    @Builder.Default
    private boolean approvedByAdmin = false;

    private LocalDateTime completedAt;

    private String employeeComment;
}
