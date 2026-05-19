package com.rwms.project.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddContributorsRequest {

    @NotEmpty
    private List<Long> userIds;
}
