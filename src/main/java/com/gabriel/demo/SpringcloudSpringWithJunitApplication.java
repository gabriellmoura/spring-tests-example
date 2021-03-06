package com.gabriel.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("com")
public class SpringcloudSpringWithJunitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringcloudSpringWithJunitApplication.class, args);
	}

}
