package com.example.back_end.configuration;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaywrightConfig {
    @Bean
    public Playwright playwright() {
        return Playwright.create();
    }
}
