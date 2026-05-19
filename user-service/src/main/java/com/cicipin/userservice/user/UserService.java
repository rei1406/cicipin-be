package com.cicipin.userservice.user;

import com.cicipin.userservice.auth.dto.RegisterRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(UUID id);

    List<UserResponse> getAllUsers();

    UserResponse createAdmin(RegisterRequest request);
}
