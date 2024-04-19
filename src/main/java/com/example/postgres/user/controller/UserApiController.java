package com.example.postgres.user.controller;

import com.example.postgres.user.dto.RegistrationUserRequest;
import com.example.postgres.user.dto.EditUserRequest;
import com.example.postgres.user.dto.UserResponse;
import com.example.postgres.user.exception.BadRequestException;
import com.example.postgres.user.exception.UserAlreadyExistException;
import com.example.postgres.user.exception.UserNotFoundException;
import com.example.postgres.user.repository.UserRepository;
import com.example.postgres.user.entity.UserEntity;
import com.example.postgres.user.routes.UserRoutes;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserApiController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${init.email}")
    private String initUser;
    @Value("${init.password}")
    private String initPassword;

    @Operation(summary = "Создание пользователя")
    @GetMapping("/")
    public UserEntity root() {
        UserEntity user = UserEntity.builder()
                .firstName("test")
                .lastName("test2")
                .build();

        user = userRepository.save(user);
        return user;
    }

    @Operation(summary = "Создание пользователя")
    @PostMapping(UserRoutes.REGISTRATION)
    public UserResponse create(@RequestBody RegistrationUserRequest request) throws BadRequestException, UserAlreadyExistException {
        request.validate();

        Optional<UserEntity> check = userRepository.findByEmail(request.getEmail());
        if (check.isPresent()) throw new UserAlreadyExistException();

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        return UserResponse.of(user);
    }

    @GetMapping(UserRoutes.BY_ID)
    public UserResponse byId(@PathVariable Long id) throws UserNotFoundException {
        return UserResponse.of(userRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    @Operation(summary = "Поиск пользователя")
    @GetMapping(UserRoutes.SEARCH)
    public List<UserResponse> search(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String query
    ) {
        Pageable pageable = PageRequest.of(page, size);

        ExampleMatcher matcher = ExampleMatcher.matchingAny()
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        Example<UserEntity> example = Example.of(
                UserEntity.builder().firstName(query).lastName(query).build(),
                matcher);
        return userRepository.findAll(example, pageable).stream().map(UserResponse::of).collect(Collectors.toList());
    }

    @Operation(summary = "Редактирование пользователя")
    @PutMapping(UserRoutes.EDIT)
    public UserResponse edit(Principal principal, @RequestBody EditUserRequest request) throws UserNotFoundException {
        UserEntity user = userRepository.findByEmail(principal.getName()).orElseThrow(UserNotFoundException::new);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = userRepository.save(user);

        return UserResponse.of(user);
    }

    @Operation(summary = "Удаление пользователя")
    @DeleteMapping(UserRoutes.BY_ID)
    public String delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return HttpStatus.OK.name();
    }

    @Operation(summary = "Инициализация пользователя")
    @GetMapping(UserRoutes.INIT)
    public UserResponse init() {
        Optional<UserEntity> checkUser = userRepository.findByEmail(initUser);
        UserEntity user;

        if (checkUser.isEmpty()) {
            user = UserEntity.builder()
                    .firstName("Default")
                    .lastName("Default")
                    .email(initUser)
                    .password(passwordEncoder.encode(initPassword))
                    .build();
            user = userRepository.save(user);
        } else  {
            user = checkUser.get();
        }
        return  UserResponse.of(user);
    }
}
