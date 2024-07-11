package com.example.back_end.configuration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfiguration {
    @Value("${user.agent}")
    private String userAgent;
    @Bean
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Codie\\Khóa luận tốt nghiệp\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent",userAgent);
//        options.addArguments("--headless");
        return new ChromeDriver(options);
    }
}
