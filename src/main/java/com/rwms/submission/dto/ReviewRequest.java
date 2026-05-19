package com.rwms.submission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotBlank
    private String action; // "APPROVE" or "REJECT"

    private String rejectionReason;
}
