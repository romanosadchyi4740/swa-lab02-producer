package com.romanosadchyi.labs.appz_lab02.controller;

import com.romanosadchyi.labs.appz_lab02.dto.GradeCreateRequest;
import com.romanosadchyi.labs.appz_lab02.dto.GradeDto;
import com.romanosadchyi.labs.appz_lab02.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<GradeDto> createGrade(@RequestBody GradeCreateRequest request) {
        GradeDto grade = gradeService.createGrade(request);
        return ResponseEntity.ok(grade);
    }
}
