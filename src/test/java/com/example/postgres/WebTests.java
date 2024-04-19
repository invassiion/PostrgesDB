package com.example.postgres;

import com.example.postgres.user.dto.RegistrationUserRequest;
import com.example.postgres.user.dto.EditUserRequest;
import com.example.postgres.user.dto.UserResponse;
import com.example.postgres.user.entity.UserEntity;
import com.example.postgres.user.repository.UserRepository;
import com.example.postgres.user.routes.UserRoutes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class WebTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Value("${init.email")
    private String initUser;

    @Value("${init.password")
    private String initPassword;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void config() {
        Optional<UserEntity> check = userRepository.findByEmail(initUser);
        if (check.isPresent()) return;

        UserEntity user = UserEntity.builder()
                .firstName("")
                .lastName("")
                .email(initUser)
                .password(passwordEncoder.encode(initPassword))
                .build();
    }

    public String authHeader() {
        return "Basic" + Base64.getEncoder().encodeToString((initUser + ":" + initPassword).getBytes());
    }

    @Test
    void contextLoad() throws Exception {
        UserEntity user = UserEntity.builder()
                .firstName("Denis")
                .lastName("Kovalinskii")
                .build();
        user = userRepository.save(user);

        mockMvc.perform(get(UserRoutes.BY_ID, user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION,authHeader()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void createTest() throws Exception {
        RegistrationUserRequest request  = RegistrationUserRequest.builder()
                .firstName("createTest")
                .lastName("createTest")
                .build();

        mockMvc.perform(
               post(UserRoutes.REGISTRATION)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andExpect(content().string(containsString("createTest")));
    }
    @Test
    void  findByIdTest() throws Exception {
        UserEntity user = UserEntity.builder()
                .firstName("findById")
                .lastName("findById")
                .build();
        userRepository.save(user);

        mockMvc.perform(get(UserRoutes.BY_ID, user.getId()).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("findById")));
    }
    @Test
    void findById_Notfound_Test() throws Exception{
        mockMvc.perform(get(UserRoutes.BY_ID, 1).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTest() throws Exception {
        UserEntity user = UserEntity.builder()
                .firstName("q")
                .lastName("q")
                .build();
        userRepository.save(user);


        EditUserRequest request  = EditUserRequest.builder()
                .id(user.getId())
                .firstName("updateTest")
                .lastName("updateTest")
                .build();


        mockMvc.perform(put(UserRoutes.BY_ID, user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(HttpHeaders.AUTHORIZATION, authHeader()))
                .andDo(print())
                .andExpect(content().string(containsString("updateTest")));
    }
    @Test
    void deleteTest() throws Exception {
        UserEntity user =  UserEntity.builder()
                .firstName("deleteTest")
                .lastName("deleteTest")
                .build();
        user = userRepository.save(user);

        mockMvc.perform(delete(UserRoutes.BY_ID,user.getId())).andDo(print()).andExpect(status().isOk());

        assert userRepository.findById(user.getId()).isEmpty();
    }

    @Test
    void searchTest() throws Exception {
        List<UserResponse> responses = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            UserEntity user = UserEntity.builder()
                    .firstName("firstName" + i)
                    .lastName("lastName" + i)
                    .build();

            user = userRepository.save(user);
            responses.add(UserResponse.of(user));
        }

        mockMvc.perform(get(UserRoutes.SEARCH).param("size", "1000").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));


    }
}
