package com.atividade.login;

import com.atividade.login.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
		+ "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
		+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
		+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration")
class LoginApplicationTests {

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	private MongoOperations mongoOperations;

	@Test
	void contextLoads() {
	}

}
