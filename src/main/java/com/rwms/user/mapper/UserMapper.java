package com.rwms.user.mapper;

import com.rwms.user.dto.CreateUserRequest;
import com.rwms.user.dto.UserResponse;
import com.rwms.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        User.Role role;
        try {
            role = (request.getRole() != null)
                    ? User.Role.valueOf(request.getRole().toUpperCase())
                    : User.Role.EMPLOYEE;
        } catch (IllegalArgumentException e) {
            role = User.Role.EMPLOYEE;
        }

        return User.builder()
                .fullName(request.getFullName())
                .employeeId(request.getEmployeeId())
                .email(request.getEmail())
                .password(request.getPassword()) // raw; encoding done in service
                .githubUsername(request.getGithubUsername())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .role(role)
                .status(User.Status.PENDING)
                .firstLogin(true)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .employeeId(user.getEmployeeId())
                .email(user.getEmail())
                .githubUsername(user.getGithubUsername())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .firstLogin(user.isFirstLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
