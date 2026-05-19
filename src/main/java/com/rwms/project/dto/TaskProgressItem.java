package com.rwms.project.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressItem {

    private Long taskId;
    private String taskName;
    private String status;
    private long completedSubtasks;
    private long totalSubtasks;
}
