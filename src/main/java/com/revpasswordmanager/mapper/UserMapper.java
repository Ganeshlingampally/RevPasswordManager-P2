package com.revpasswordmanager.mapper;

import com.revpasswordmanager.dto.RegisterRequest;
import com.revpasswordmanager.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setTwoFaEnabled(false);
        return user;
    }
}
