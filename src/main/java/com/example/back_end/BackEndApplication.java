package com.example.back_end;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackEndApplication.class, args);
	}

}
