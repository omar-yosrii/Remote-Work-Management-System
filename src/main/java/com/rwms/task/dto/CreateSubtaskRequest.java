package com.rwms.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubtaskRequest {

    @NotBlank
    private String name;

    private String description;
}
