package com.liban.orderplatform.auth.mapper;

import com.liban.orderplatform.auth.dto.RegisterRequest;
import com.liban.orderplatform.auth.dto.UserResponse;
import com.liban.orderplatform.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(RegisterRequest request) {
        User user = new User();

        user.setEmail(request.email());

        return user;
    }

    public UserResponse toUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
