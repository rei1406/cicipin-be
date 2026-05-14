package com.cicipin.userservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: GET    /me      - get current user profile
    // TODO: PUT    /me      - update current user profile
    // TODO: DELETE /me      - deactivate current user account
    // TODO: GET    /{id}    - get user by id (admin)
    // TODO: GET    /        - list all users (admin)
}
