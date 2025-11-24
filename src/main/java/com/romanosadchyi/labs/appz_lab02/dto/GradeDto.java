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
    private Long studentId;
    private Long teacherId;
    private Long parentId;

    public static GradeDto gradeToDto(Grade grade) {
        return new GradeDto(
                grade.getId(),
                grade.getValue(),
                grade.getStudentId(),
                grade.getTeacherId(),
                grade.getParentId()
        );
    }
}
