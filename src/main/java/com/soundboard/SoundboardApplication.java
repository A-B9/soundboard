package com.soundboard;

import com.soundboard.util.TestComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/// convenience annotation that adds a @Configuration, @EnableAutoConfiguration, and @ComponentScan
/// where @Configuration indicates that the class can be used by the Spring IoC container as a source of bean definitions, IoC means Inversion of Control
/// @EnableAutoConfiguration tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings
/// @ComponentScan tells Spring to look for other components, configurations, and services in the com/soundboard/soundboard package, allowing it to find the controllers
@SpringBootApplication
@EnableJpaRepositories("com.soundboard.soundboard.repository")
public class SoundboardApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SoundboardApplication.class, args);
		
		TestComponent obj = context.getBean(TestComponent.class);
		obj.build();
	}

}
