package com.rwms.submission.dto;

import com.rwms.task.dto.SubtaskResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDetailResponse {

    private SubmissionResponse submissionInfo;
    private List<SubtaskResponse> subtasks;
    private String accomplishmentComment;
    private String attachmentPath;
    private String alternativeGithubLink;
}
