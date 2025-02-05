package com.config;

import org.springframework.context.annotation.Bean;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@Configuration
public class AppConfig {
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}
