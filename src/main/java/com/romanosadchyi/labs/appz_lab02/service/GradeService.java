package com.romanosadchyi.labs.appz_lab02.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.romanosadchyi.labs.appz_lab02.dto.GradeCreateRequest;
import com.romanosadchyi.labs.appz_lab02.dto.GradeDto;
import com.romanosadchyi.labs.appz_lab02.dto.GradeMessage;
import com.romanosadchyi.labs.appz_lab02.dto.GradeViewDto;
import com.romanosadchyi.labs.appz_lab02.dto.LogDto;
import com.romanosadchyi.labs.appz_lab02.dto.TeacherDto;
import com.romanosadchyi.labs.appz_lab02.dto.UserDto;
import com.romanosadchyi.labs.appz_lab02.model.Grade;
import com.romanosadchyi.labs.appz_lab02.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {
    private final GradeRepository gradeRepository;
    private final UserServiceClient userServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationServiceClient notificationServiceClient;
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
        GradeMessage message = new GradeMessage();
        message.setGrade(gradeDto);
        message.setParentEmail(parentEmail);
        message.setFromRest(false);

        try {
            String json = objectMapper.writeValueAsString(message);
            String logJson = objectMapper.writeValueAsString(
                    new LogDto(String.format("New grade posted: %s", json), LocalDateTime.now()));

            rabbitTemplate.convertAndSend(exchangeName, routingKeyLog, logJson);
            
            rabbitTemplate.convertAndSend(exchangeName, routingKeyNotification, json);
            log.info("RabbitMQ notification sent at: {} ms", System.currentTimeMillis());

            GradeMessage restMessage = new GradeMessage();
            restMessage.setGrade(gradeDto);
            restMessage.setParentEmail(parentEmail);
            restMessage.setFromRest(true);
            
            notificationServiceClient.sendNotificationViaRest(restMessage);
            log.info("REST notification sent at: {} ms", System.currentTimeMillis());
        } catch (JsonProcessingException e) {
            log.error("Error converting grade to JSON: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
        }

        return gradeDto;
    }

    public List<GradeViewDto> getGradesByStudentId(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return grades.stream()
                .map(this::mapToGradeViewDto)
                .toList();
    }

    public List<GradeViewDto> getGradesByParentId(Long parentId) {
        List<Grade> grades = gradeRepository.findByParentId(parentId);
        return grades.stream()
                .map(this::mapToGradeViewDto)
                .toList();
    }

    public List<GradeViewDto> getGradesByStudentIdAndSubject(Long studentId, String subject) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return grades.stream()
                .map(this::mapToGradeViewDto)
                .filter(grade -> grade.getSubject().equals(subject))
                .toList();
    }

    private GradeViewDto mapToGradeViewDto(Grade grade) {
        GradeViewDto dto = new GradeViewDto();
        dto.setId(grade.getId());
        dto.setValue(grade.getValue());
        dto.setStudentId(grade.getStudentId());
        dto.setTeacherId(grade.getTeacherId());
        dto.setCreatedAt(grade.getCreatedAt());

        try {
            UserDto student = userServiceClient.getUserById(grade.getStudentId());
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());

            TeacherDto teacher = userServiceClient.getTeacherById(grade.getTeacherId());
            dto.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
            dto.setSubject(teacher.getSubject());
        } catch (Exception e) {
            log.error("Error fetching user details for grade {}: {}", grade.getId(), e.getMessage());
            dto.setStudentName("Unknown");
            dto.setTeacherName("Unknown");
            dto.setSubject("Unknown");
        }

        return dto;
    }
}
