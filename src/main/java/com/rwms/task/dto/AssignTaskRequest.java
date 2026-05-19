package com.rwms.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTaskRequest {

    @NotNull
    private Long employeeId;
}
