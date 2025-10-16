package com.romanosadchyi.labs.appz_lab02.service;

import com.romanosadchyi.labs.appz_lab02.dto.GradeCreateRequest;
import com.romanosadchyi.labs.appz_lab02.dto.GradeDto;
import com.romanosadchyi.labs.appz_lab02.model.Grade;
import com.romanosadchyi.labs.appz_lab02.model.Student;
import com.romanosadchyi.labs.appz_lab02.model.Teacher;
import com.romanosadchyi.labs.appz_lab02.repository.GradeRepository;
import com.romanosadchyi.labs.appz_lab02.repository.StudentRepository;
import com.romanosadchyi.labs.appz_lab02.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public GradeDto createGrade(GradeCreateRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setTeacher(teacher);
        grade.setValue(request.getValue());

        return GradeDto.gradeToDto(gradeRepository.save(grade));
    }
}
