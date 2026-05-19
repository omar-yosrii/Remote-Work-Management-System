package com.rwms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdminRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String employeeId;

    @Email
    @NotBlank
    private String gmailAddress;

    private String githubUsername;
}
