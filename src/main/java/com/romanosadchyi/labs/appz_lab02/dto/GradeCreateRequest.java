package com.romanosadchyi.labs.appz_lab02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeCreateRequest {
    private Long studentId;
    private Long teacherId;
    private Double value;
}
