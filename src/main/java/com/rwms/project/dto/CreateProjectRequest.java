package com.rwms.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String department;

    private String description;
}
