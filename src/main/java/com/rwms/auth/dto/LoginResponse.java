package com.rwms.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String role;
    private boolean firstLogin;
    private Long userId;
}
