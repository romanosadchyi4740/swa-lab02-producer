package com.romanosadchyi.labs.appz_lab02.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.romanosadchyi.labs.appz_lab02.dto.GradeCreateRequest;
import com.romanosadchyi.labs.appz_lab02.dto.GradeDto;
import com.romanosadchyi.labs.appz_lab02.dto.GradeMessage;
import com.romanosadchyi.labs.appz_lab02.dto.LogDto;
import com.romanosadchyi.labs.appz_lab02.model.Grade;
import com.romanosadchyi.labs.appz_lab02.model.Student;
import com.romanosadchyi.labs.appz_lab02.model.Teacher;
import com.romanosadchyi.labs.appz_lab02.repository.GradeRepository;
import com.romanosadchyi.labs.appz_lab02.repository.StudentRepository;
import com.romanosadchyi.labs.appz_lab02.repository.TeacherRepository;
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
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
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
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setTeacher(teacher);
        grade.setValue(request.getValue());

        Grade savedGrade = gradeRepository.save(grade);
        GradeDto gradeDto = GradeDto.gradeToDto(savedGrade);

        String parentEmail = student.getParent().getEmail();
        GradeMessage message = new GradeMessage(gradeDto, parentEmail);

        try {
            String json = objectMapper.writeValueAsString(message);
            String logJson = objectMapper.writeValueAsString(
                    new LogDto(String.format("New grade posted: %s", json), LocalDateTime.now()));

            rabbitTemplate.convertAndSend(exchangeName, routingKeyLog, logJson);
            rabbitTemplate.convertAndSend(exchangeName, routingKeyNotification, json);
        } catch (JsonProcessingException e) {
            log.error("Error converting grade to JSON: {}", e.getMessage());
            rabbitTemplate.convertAndSend("Error converting grade to JSON: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return gradeDto;
    }
}
