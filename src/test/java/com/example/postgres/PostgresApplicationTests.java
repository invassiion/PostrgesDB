package com.example.postgres;

import com.example.postgres.user.dto.UserResponse;
import com.example.postgres.user.entity.UserEntity;
import com.example.postgres.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PostgresApplicationTests {
@Autowired
private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	void repositoryTest() {
		UserEntity user = UserEntity.builder()
				.lastName("Denis")
				.firstName("Kovalinskii")
				.build();

		user = userRepository.save(user);

		UserEntity check = userRepository.findById(user.getId()).get();

		assert check.getId().equals(user.getId());
	}


}
