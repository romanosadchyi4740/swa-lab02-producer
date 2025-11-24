package com.romanosadchyi.labs.appz_lab02.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.romanosadchyi.labs.appz_lab02.dto.GradeCreateRequest;
import com.romanosadchyi.labs.appz_lab02.dto.GradeDto;
import com.romanosadchyi.labs.appz_lab02.dto.GradeMessage;
import com.romanosadchyi.labs.appz_lab02.dto.LogDto;
import com.romanosadchyi.labs.appz_lab02.dto.UserDto;
import com.romanosadchyi.labs.appz_lab02.model.Grade;
import com.romanosadchyi.labs.appz_lab02.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {
    private final GradeRepository gradeRepository;
    private final UserServiceClient userServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.log}")
    private String routingKeyLog;

    @Value("${rabbitmq.routing-key.notification}")
    private String routingKeyNotification;

    public GradeDto createGrade(GradeCreateRequest request) {
        // Validate that parent exists by fetching from user-service
        UserDto parent = userServiceClient.getUserById(request.getParentId());
        if (parent == null) {
            throw new RuntimeException("Parent not found");
        }

        Grade grade = new Grade();
        grade.setStudentId(request.getStudentId());
        grade.setTeacherId(request.getTeacherId());
        grade.setParentId(request.getParentId());
        grade.setValue(request.getValue());

        Grade savedGrade = gradeRepository.save(grade);
        GradeDto gradeDto = GradeDto.gradeToDto(savedGrade);

        String parentEmail = parent.getEmail();
        GradeMessage message = new GradeMessage(gradeDto, parentEmail);

        try {
            String json = objectMapper.writeValueAsString(message);
            String logJson = objectMapper.writeValueAsString(
                    new LogDto(String.format("New grade posted: %s", json), LocalDateTime.now()));

            rabbitTemplate.convertAndSend(exchangeName, routingKeyLog, logJson);
            rabbitTemplate.convertAndSend(exchangeName, routingKeyNotification, json);
        } catch (JsonProcessingException e) {
            log.error("Error converting grade to JSON: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return gradeDto;
    }
}
