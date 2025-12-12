package com.soundboard.soundboard;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

/// convenience annotatoin that adds a @Configuration, @EnableAutoConfiguration, and @ComponentScan
/// where @Configuration indicates that the class can be used by the Spring IoC container as a source of bean definitions, IoC means Inversion of Control
/// @EnableAutoConfiguration tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings
/// @ComponentScan tells Spring to look for other components, configurations, and services in the com/soundboard/soundboard package, allowing it to find the controllers
@SpringBootApplication
@EnableJpaRepositories("com.soundboard.soundboard.repository")
public class SoundboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoundboardApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext context) {
		return args -> {
			System.out.println("Lets inspect the beans provided by Spring Boot:");

			String[] beanNames = context.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};
	}

}
