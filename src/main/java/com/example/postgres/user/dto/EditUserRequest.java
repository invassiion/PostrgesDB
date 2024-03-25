package com.example.postgres.user.dto;

import lombok.Getter;

@Getter
public class EditUserRequest {
    private Long id;
    private String firstName;
    private String lastName;
}
