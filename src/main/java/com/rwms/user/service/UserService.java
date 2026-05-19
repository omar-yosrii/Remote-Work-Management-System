package com.rwms.user.service;

import com.rwms.user.dto.CreateUserRequest;
import com.rwms.user.dto.UpdateUserRequest;
import com.rwms.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Override
    public List<UserResponse> getAllUsers() {
        return List.of();
    }

    @Override
    public UserResponse getUserById(Long id) {
        return null;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        return null;
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        // No-op
    }
}