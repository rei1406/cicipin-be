package com.cicipin.userservice.user;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(UUID id);

    List<UserResponse> getAllUsers();
}
