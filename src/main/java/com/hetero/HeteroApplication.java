package com.hetero;

import com.hetero.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class HeteroApplication {


	private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public static void main(String[] args) {

		SpringApplication.run(HeteroApplication.class, args);

	}

}
