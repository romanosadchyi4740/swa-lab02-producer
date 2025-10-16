package com.romanosadchyi.labs.appz_lab02.repository;

import com.romanosadchyi.labs.appz_lab02.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}
