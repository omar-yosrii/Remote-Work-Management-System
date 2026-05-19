package com.rwms.user.mapper;

import com.rwms.user.dto.CreateUserRequest;
import com.rwms.user.dto.UserResponse;
import com.rwms.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        return null;
    }

    public UserResponse toResponse(User user) {
        return null;
    }
}
