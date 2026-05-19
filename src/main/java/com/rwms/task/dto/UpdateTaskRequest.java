package com.rwms.task.dto;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    private String name;

    private String description;

    @FutureOrPresent
    private LocalDate deadline;

    private String githubRepoLink;
}
