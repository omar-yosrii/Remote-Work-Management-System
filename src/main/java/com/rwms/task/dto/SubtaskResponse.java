package com.rwms.task.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskResponse {

    private Long id;
    private String name;
    private boolean completedByEmployee;
    private boolean approvedByAdmin;
    private LocalDateTime completedAt;
    private String employeeComment;
}
