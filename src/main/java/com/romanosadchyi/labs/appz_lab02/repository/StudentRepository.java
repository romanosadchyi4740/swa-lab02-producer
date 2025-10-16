package com.romanosadchyi.labs.appz_lab02.repository;

import com.romanosadchyi.labs.appz_lab02.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
