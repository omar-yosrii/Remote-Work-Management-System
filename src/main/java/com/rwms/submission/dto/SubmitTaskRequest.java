package com.rwms.submission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTaskRequest {

    @NotBlank
    private String accomplishmentComment;

    private String alternativeGithubLink;
}
