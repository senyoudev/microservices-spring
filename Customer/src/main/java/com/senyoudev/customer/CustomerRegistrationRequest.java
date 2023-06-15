package com.senyoudev.customer;

public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
}