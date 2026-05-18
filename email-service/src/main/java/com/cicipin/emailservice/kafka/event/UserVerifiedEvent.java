package com.cicipin.emailservice.kafka.event;

public record UserVerifiedEvent(String email, String username, String name) {
}
