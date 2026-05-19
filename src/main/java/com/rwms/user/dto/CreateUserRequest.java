package com.rwms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String employeeId;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String githubUsername;

    private String phone;

    private String department;

    private String role;
}
