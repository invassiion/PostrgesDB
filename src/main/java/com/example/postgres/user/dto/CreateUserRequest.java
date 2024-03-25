package com.example.postgres.user.dto;


import lombok.Getter;

@Getter
public class CreateUserRequest {
    private String firstName;
    private String lastName;
}
