package com.rwms.auth.controller;

import com.rwms.auth.dto.ChangePasswordRequest;
import com.rwms.auth.dto.LoginRequest;
import com.rwms.auth.dto.LoginResponse;
import com.rwms.auth.dto.RegisterAdminRequest;
import com.rwms.auth.service.AuthService;
import com.rwms.user.dto.UserResponse;
import com.rwms.user.entity.User;
import com.rwms.user.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    /**
     * POST /auth/login
     * Public endpoint — returns JWT token on valid credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /auth/register
     * Public endpoint — self-registration creates a PENDING ADMIN account.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterAdminRequest request) {
        authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * POST /auth/change-password
     * Requires valid JWT — changes password and sets firstLogin = false.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        authService.changePassword(request, currentUser.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /auth/me
     * Requires valid JWT — returns the authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userMapper.toResponse(currentUser));
    }
}
