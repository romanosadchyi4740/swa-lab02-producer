package com.romanosadchyi.labs.appz_lab02.controller;

import com.romanosadchyi.labs.appz_lab02.dto.GradeCreateRequest;
import com.romanosadchyi.labs.appz_lab02.dto.GradeDto;
import com.romanosadchyi.labs.appz_lab02.dto.GradeViewDto;
import com.romanosadchyi.labs.appz_lab02.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {
    private final GradeService gradeService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GradeDto> createGrade(@RequestBody GradeCreateRequest request) {
        GradeDto grade = gradeService.createGrade(request);
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeViewDto>> getGradesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.getGradesByStudentId(studentId));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<GradeViewDto>> getGradesByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(gradeService.getGradesByParentId(parentId));
    }

    @GetMapping("/student/{studentId}/subject/{subject}")
    public ResponseEntity<List<GradeViewDto>> getGradesByStudentIdAndSubject(
            @PathVariable Long studentId,
            @PathVariable String subject) {
        return ResponseEntity.ok(gradeService.getGradesByStudentIdAndSubject(studentId, subject));
    }
}
