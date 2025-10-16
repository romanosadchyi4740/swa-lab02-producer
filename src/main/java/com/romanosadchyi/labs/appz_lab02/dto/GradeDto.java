package com.romanosadchyi.labs.appz_lab02.dto;

import com.romanosadchyi.labs.appz_lab02.model.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {
    private Long id;
    private Double value;
    private String studentName;
    private String teacherName;

    public static GradeDto gradeToDto(Grade grade) {
        return new GradeDto(grade.getId(),
                grade.getValue(),
                grade.getStudent().getFirstName(),
                grade.getTeacher().getFirstName());
    }
}
