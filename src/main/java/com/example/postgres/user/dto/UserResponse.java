package com.example.postgres.user.dto;

import com.example.postgres.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserResponse extends UserEntity {
    public static UserResponse of(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

}
