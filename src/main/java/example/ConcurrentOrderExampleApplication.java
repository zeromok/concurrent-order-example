package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "example.V0.repository")
public class ConcurrentOrderExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrentOrderExampleApplication.class, args);
	}

}
