package com.rwms.auth.service;

import com.rwms.auth.dto.ChangePasswordRequest;
import com.rwms.auth.dto.LoginRequest;
import com.rwms.auth.dto.LoginResponse;
import com.rwms.auth.dto.RegisterAdminRequest;
import com.rwms.auth.util.JwtUtil;
import com.rwms.common.exception.AccountDisabledException;
import com.rwms.common.exception.AccountPendingException;
import com.rwms.common.exception.DuplicateEntityException;
import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.rwms.notification.dto.NotificationEvent;
import com.rwms.notification.entity.NotificationType;
import com.rwms.audit.service.AuditLogService;
import com.rwms.audit.command.*;

import java.security.SecureRandom;
import java.util.List;

@Service
public class AuthService {

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       ApplicationEventPublisher eventPublisher,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.eventPublisher = eventPublisher;
        this.auditLogService = auditLogService;
    }

    /**
     * Authenticates a user and returns a JWT token with login metadata.
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (user.getStatus() == User.Status.PENDING) {
            throw new AccountPendingException();
        }

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new AccountDisabledException("Your account has been deactivated. Contact your manager.");
        }

        String token = jwtUtil.generateToken(user);

        auditLogService.log(new UserLoginCommand(user, user.getEmail(), "Successful login"));

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .firstLogin(user.isFirstLogin())
                .userId(user.getId())
                .build();
    }

    /**
     * Self-registration: creates a PENDING ADMIN account with a temporary password.
     */
    public void registerAdmin(RegisterAdminRequest request) {
        if (userRepository.existsByEmail(request.getGmailAddress())) {
            throw new DuplicateEntityException("Email already registered: " + request.getGmailAddress());
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateEntityException("Employee ID already registered: " + request.getEmployeeId());
        }

        String tempPassword = generateTempPassword(8);

        User user = User.builder()
                .fullName(request.getFullName())
                .employeeId(request.getEmployeeId())
                .email(request.getGmailAddress())
                .githubUsername(request.getGithubUsername())
                .password(passwordEncoder.encode(tempPassword))
                .role(User.Role.ADMIN)
                .status(User.Status.PENDING)
                .firstLogin(true)
                .build();

        userRepository.save(user);

        // Notify managers
        List<User> managers = userRepository.findByRole(User.Role.MANAGER);
        for (User manager : managers) {
            eventPublisher.publishEvent(NotificationEvent.builder()
                    .recipient(manager)
                    .title("New Admin Registration")
                    .message("A new admin (" + request.getFullName() + ") has registered and is pending approval.")
                    .type(NotificationType.NEW_ADMIN_REGISTRATION)
                    .build());
        }

        auditLogService.log(new AccountCreatedCommand(user, user.getEmail(), "Admin account created and pending approval"));

        // TODO: In production, send tempPassword via email notification
        System.out.println("[RWMS] Temporary password for " + request.getGmailAddress() + ": " + tempPassword);
    }

    /**
     * Changes the password for the currently authenticated user.
     */
    public void changePassword(ChangePasswordRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);

        auditLogService.log(new PasswordChangedCommand(user, user.getEmail(), "Password changed"));
    }

    private String generateTempPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
