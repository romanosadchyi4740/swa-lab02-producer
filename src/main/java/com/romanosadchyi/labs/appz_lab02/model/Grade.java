package com.romanosadchyi.labs.appz_lab02.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double value;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "parent_id")
    private Long parentId;
}
