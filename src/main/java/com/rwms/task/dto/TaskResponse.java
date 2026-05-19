package com.rwms.task.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private String githubRepoLink;
    private String status;
    private String assignedEmployeeName;
    private String projectName;
    private List<SubtaskResponse> subtasks;
}
