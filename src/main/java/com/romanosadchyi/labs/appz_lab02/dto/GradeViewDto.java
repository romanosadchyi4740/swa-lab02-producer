package com.romanosadchyi.labs.appz_lab02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeViewDto {
    private Long id;
    private Double value;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private String subject;
}

