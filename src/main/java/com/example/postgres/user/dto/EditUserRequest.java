package com.example.postgres.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EditUserRequest {
    private Long id;
    private String firstName;
    private String lastName;
}
