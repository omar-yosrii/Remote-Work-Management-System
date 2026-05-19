package com.rwms.project.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressResponse {

    private Long projectId;
    private String projectName;
    private long totalTasks;
    private long completedTasks;
    private long totalSubtasks;
    private long approvedSubtasks;
    private List<TaskProgressItem> tasks;
}
