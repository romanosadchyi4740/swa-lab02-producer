package com.romanosadchyi.labs.appz_lab02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogDto {
    private final String serviceName = "grade-service";
    private String message;
    private LocalDateTime timestamp;
}
