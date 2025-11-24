package com.romanosadchyi.labs.appz_lab02.service;

import com.romanosadchyi.labs.appz_lab02.dto.TeacherDto;
import com.romanosadchyi.labs.appz_lab02.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {
    private final RestClient restClient;

    @Value("${user-service.url:http://localhost:8084}")
    private String userServiceUrl;

    public UserDto getUserById(Long id) {
        try {
            String url = userServiceUrl + "/api/users/" + id;

            return restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(UserDto.class);

        } catch (Exception e) {
            log.error("Error fetching user from user-service: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch user from user-service", e);
        }
    }

    public TeacherDto getTeacherById(Long id) {
        try {
            String url = userServiceUrl + "/api/users/teachers/" + id;

            return restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TeacherDto.class);

        } catch (Exception e) {
            log.error("Error fetching teacher from user-service: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch teacher from user-service", e);
        }
    }
}
