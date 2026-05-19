package com.rwms.user.service;

import com.rwms.user.dto.CreateUserRequest;
import com.rwms.user.dto.UpdateUserRequest;
import com.rwms.user.dto.UserResponse;

import java.util.List;

public interface IUserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getUsersByDepartment(String department);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
