package com.rwms.task.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @FutureOrPresent
    private LocalDate deadline;

    private String githubRepoLink;

    private List<CreateSubtaskRequest> subtasks;
}
