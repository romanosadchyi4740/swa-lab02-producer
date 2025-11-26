package com.romanosadchyi.labs.appz_lab02.service;

import com.romanosadchyi.labs.appz_lab02.dto.GradeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceClient {
    private final WebClient webClient;

    @Value("${notification-service.url:http://localhost:8082}")
    private String notificationServiceUrl;

    public void sendNotificationViaRest(GradeMessage message) {
        String url = notificationServiceUrl + "/api/notifications/grade";
        
        // Send asynchronously (fire and forget) - don't wait for response
        webClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        response -> log.info("Notification sent via REST to notification service (async)"),
                        error -> log.error("Error sending notification via REST (async): {}", error.getMessage())
                );
        
        log.info("REST notification request dispatched asynchronously");
    }
}
