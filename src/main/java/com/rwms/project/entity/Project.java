package com.rwms.project.entity;

import com.rwms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true)
    private String descriptionPdfPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_leader_id", nullable = true)
    private User teamLeader;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_contributors",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> contributors = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
