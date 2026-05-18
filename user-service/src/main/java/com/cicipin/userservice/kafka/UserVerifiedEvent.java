package com.cicipin.userservice.kafka;

public record UserVerifiedEvent(String email, String username, String name) {
}
