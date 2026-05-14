package com.cicipin.userservice.auth;

import com.cicipin.userservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    // TODO: implement register
    // TODO: implement login
    // TODO: implement logout
    // TODO: implement refreshToken
    // TODO: implement verifyEmail
}
