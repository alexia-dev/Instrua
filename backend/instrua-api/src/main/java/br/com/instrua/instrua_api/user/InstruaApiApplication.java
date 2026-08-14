package br.com.instrua.instrua_api.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.instrua.instrua_api")
public class InstruaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstruaApiApplication.class, args);
	}

}
