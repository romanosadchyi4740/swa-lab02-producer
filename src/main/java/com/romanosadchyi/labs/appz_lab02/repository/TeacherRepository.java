package com.romanosadchyi.labs.appz_lab02.repository;

import com.romanosadchyi.labs.appz_lab02.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
