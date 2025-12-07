package com.romanosadchyi.labs.appz_lab02.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.romanosadchyi.labs.appz_lab02.dto.*;
import com.romanosadchyi.labs.appz_lab02.model.Grade;
import com.romanosadchyi.labs.appz_lab02.repository.GradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeService Unit Tests")
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private GradeService gradeService;

    private GradeCreateRequest gradeCreateRequest;
    private UserDto parentUser;
    private Grade savedGrade;
    private UserDto studentUser;
    private TeacherDto teacherDto;

    @BeforeEach
    void setUp() {
        // Set up properties using reflection
        ReflectionTestUtils.setField(gradeService, "exchangeName", "test-exchange");
        ReflectionTestUtils.setField(gradeService, "routingKeyLog", "log.routing.key");
        ReflectionTestUtils.setField(gradeService, "routingKeyNotification", "notification.routing.key");

        // Initialize test data
        gradeCreateRequest = new GradeCreateRequest();
        gradeCreateRequest.setStudentId(1L);
        gradeCreateRequest.setTeacherId(2L);
        gradeCreateRequest.setParentId(3L);
        gradeCreateRequest.setValue(85.5);

        parentUser = new UserDto();
        parentUser.setId(3L);
        parentUser.setEmail("parent@example.com");
        parentUser.setFirstName("John");
        parentUser.setLastName("Parent");

        savedGrade = new Grade();
        savedGrade.setId(1L);
        savedGrade.setStudentId(1L);
        savedGrade.setTeacherId(2L);
        savedGrade.setParentId(3L);
        savedGrade.setValue(85.5);
        savedGrade.setCreatedAt(LocalDateTime.now());

        studentUser = new UserDto();
        studentUser.setId(1L);
        studentUser.setFirstName("Jane");
        studentUser.setLastName("Student");
        studentUser.setEmail("student@example.com");

        teacherDto = new TeacherDto();
        teacherDto.setId(2L);
        teacherDto.setFirstName("Dr. Smith");
        teacherDto.setLastName("Teacher");
        teacherDto.setSubject("Mathematics");
    }

    @Test
    @DisplayName("Should create grade successfully")
    void shouldCreateGradeSuccessfully() throws JsonProcessingException {
        // Given
        when(userServiceClient.getUserById(3L)).thenReturn(parentUser);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        // When
        GradeDto result = gradeService.createGrade(gradeCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals(savedGrade.getId(), result.getId());
        assertEquals(savedGrade.getValue(), result.getValue());
        assertEquals(savedGrade.getStudentId(), result.getStudentId());
        assertEquals(savedGrade.getTeacherId(), result.getTeacherId());
        assertEquals(savedGrade.getParentId(), result.getParentId());

        verify(userServiceClient).getUserById(3L);
        verify(gradeRepository).save(any(Grade.class));
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), anyString());
        verify(notificationServiceClient).sendNotificationViaRest(any(GradeMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when parent not found")
    void shouldThrowExceptionWhenParentNotFound() {
        // Given
        when(userServiceClient.getUserById(3L)).thenReturn(null);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gradeService.createGrade(gradeCreateRequest);
        });

        assertEquals("Parent not found", exception.getMessage());
        verify(userServiceClient).getUserById(3L);
        verify(gradeRepository, never()).save(any(Grade.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
        verify(notificationServiceClient, never()).sendNotificationViaRest(any(GradeMessage.class));
    }

    @Test
    @DisplayName("Should send correct RabbitMQ messages when creating grade")
    void shouldSendCorrectRabbitMQMessages() throws JsonProcessingException {
        // Given
        when(userServiceClient.getUserById(3L)).thenReturn(parentUser);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> notificationCaptor = ArgumentCaptor.forClass(String.class);

        // When
        gradeService.createGrade(gradeCreateRequest);

        // Then
        verify(rabbitTemplate, times(2)).convertAndSend(eq("test-exchange"), anyString(), anyString());
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("log.routing.key"), logCaptor.capture());
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("notification.routing.key"), notificationCaptor.capture());

        String logMessage = logCaptor.getValue();
        String notificationMessage = notificationCaptor.getValue();

        assertNotNull(logMessage);
        assertNotNull(notificationMessage);
        assertTrue(logMessage.contains("New grade posted"));
        assertTrue(notificationMessage.contains("\"parentEmail\""));
    }

    @Test
    @DisplayName("Should send REST notification when creating grade")
    void shouldSendRESTNotification() {
        // Given
        when(userServiceClient.getUserById(3L)).thenReturn(parentUser);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        ArgumentCaptor<GradeMessage> messageCaptor = ArgumentCaptor.forClass(GradeMessage.class);

        // When
        gradeService.createGrade(gradeCreateRequest);

        // Then
        verify(notificationServiceClient).sendNotificationViaRest(messageCaptor.capture());
        GradeMessage sentMessage = messageCaptor.getValue();

        assertNotNull(sentMessage);
        assertNotNull(sentMessage.getGrade());
        assertEquals(parentUser.getEmail(), sentMessage.getParentEmail());
        assertTrue(sentMessage.getFromRest());
        assertEquals(savedGrade.getId(), sentMessage.getGrade().getId());
    }

    @Test
    @DisplayName("Should continue processing even if REST notification fails")
    void shouldContinueProcessingWhenRESTNotificationFails() {
        // Given
        when(userServiceClient.getUserById(3L)).thenReturn(parentUser);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);
        doThrow(new RuntimeException("REST error")).when(notificationServiceClient).sendNotificationViaRest(any(GradeMessage.class));

        // When
        GradeDto result = gradeService.createGrade(gradeCreateRequest);

        // Then - Should still return the grade even if notification fails
        assertNotNull(result);
        verify(gradeRepository).save(any(Grade.class));
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should get grades by student ID successfully")
    void shouldGetGradesByStudentIdSuccessfully() {
        // Given
        List<Grade> grades = Arrays.asList(savedGrade, createTestGrade(2L, 1L, 90.0));
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);
        when(userServiceClient.getUserById(1L)).thenReturn(studentUser);
        when(userServiceClient.getTeacherById(2L)).thenReturn(teacherDto);

        // When
        List<GradeViewDto> result = gradeService.getGradesByStudentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getStudentId());
        verify(gradeRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Should get grades by parent ID successfully")
    void shouldGetGradesByParentIdSuccessfully() {
        // Given
        List<Grade> grades = Arrays.asList(savedGrade);
        when(gradeRepository.findByParentId(3L)).thenReturn(grades);
        when(userServiceClient.getUserById(1L)).thenReturn(studentUser);
        when(userServiceClient.getTeacherById(2L)).thenReturn(teacherDto);

        // When
        List<GradeViewDto> result = gradeService.getGradesByParentId(3L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, savedGrade.getParentId());
        verify(gradeRepository).findByParentId(3L);
    }

    @Test
    @DisplayName("Should handle exception when fetching user details in mapToGradeViewDto")
    void shouldHandleExceptionWhenFetchingUserDetails() {
        // Given
        List<Grade> grades = Arrays.asList(savedGrade);
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);
        when(userServiceClient.getUserById(1L)).thenThrow(new RuntimeException("User service error"));

        // When
        List<GradeViewDto> result = gradeService.getGradesByStudentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).getStudentName());
        assertEquals("Unknown", result.get(0).getTeacherName());
        assertEquals("Unknown", result.get(0).getSubject());
    }

    @Test
    @DisplayName("Should return empty list when no grades found for student")
    void shouldReturnEmptyListWhenNoGradesFoundForStudent() {
        // Given
        when(gradeRepository.findByStudentId(1L)).thenReturn(List.of());

        // When
        List<GradeViewDto> result = gradeService.getGradesByStudentId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gradeRepository).findByStudentId(1L);
        verify(userServiceClient, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("Should return empty list when no grades found for parent")
    void shouldReturnEmptyListWhenNoGradesFoundForParent() {
        // Given
        when(gradeRepository.findByParentId(3L)).thenReturn(List.of());

        // When
        List<GradeViewDto> result = gradeService.getGradesByParentId(3L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gradeRepository).findByParentId(3L);
    }

    @Test
    @DisplayName("Should map grade to view DTO with all fields correctly")
    void shouldMapGradeToViewDtoCorrectly() {
        // Given
        List<Grade> grades = Arrays.asList(savedGrade);
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);
        when(userServiceClient.getUserById(1L)).thenReturn(studentUser);
        when(userServiceClient.getTeacherById(2L)).thenReturn(teacherDto);

        // When
        List<GradeViewDto> result = gradeService.getGradesByStudentId(1L);

        // Then
        assertEquals(1, result.size());
        GradeViewDto dto = result.get(0);
        assertEquals(savedGrade.getId(), dto.getId());
        assertEquals(savedGrade.getValue(), dto.getValue());
        assertEquals(savedGrade.getStudentId(), dto.getStudentId());
        assertEquals(savedGrade.getTeacherId(), dto.getTeacherId());
        assertEquals(savedGrade.getCreatedAt(), dto.getCreatedAt());
        assertEquals("Jane Student", dto.getStudentName());
        assertEquals("Dr. Smith Teacher", dto.getTeacherName());
        assertEquals("Mathematics", dto.getSubject());
    }

    private Grade createTestGrade(Long id, Long studentId, Double value) {
        Grade grade = new Grade();
        grade.setId(id);
        grade.setStudentId(studentId);
        grade.setTeacherId(2L);
        grade.setParentId(3L);
        grade.setValue(value);
        grade.setCreatedAt(LocalDateTime.now());
        return grade;
    }

}

