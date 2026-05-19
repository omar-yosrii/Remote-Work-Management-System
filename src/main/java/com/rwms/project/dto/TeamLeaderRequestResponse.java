package com.rwms.project.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamLeaderRequestResponse {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private Long projectId;
    private String projectName;
    private String status;
    private LocalDateTime submittedAt;
}
