package com.rwms.project.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String department;
    private String description;
    private String teamLeaderName;
    private int contributorCount;
    private int taskCount;
    private int completedTaskCount;
}
