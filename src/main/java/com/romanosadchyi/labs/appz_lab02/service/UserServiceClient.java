package com.romanosadchyi.labs.appz_lab02.service;

import com.romanosadchyi.labs.appz_lab02.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UserServiceClient {
    private final RestTemplate restTemplate;
    
    @Value("${user-service.url:http://localhost:8084}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserDto getUserById(Long id) {
        try {
            String url = userServiceUrl + "/api/users/" + id;
            return restTemplate.getForObject(url, UserDto.class);
        } catch (Exception e) {
            log.error("Error fetching user from user-service: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch user from user-service", e);
        }
    }
}

