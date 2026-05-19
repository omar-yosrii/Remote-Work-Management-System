package com.rwms.submission.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String authorName;
    private String content;
    private boolean isPrivateNote;
    private LocalDateTime createdAt;
}
