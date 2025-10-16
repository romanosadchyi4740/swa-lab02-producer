package com.romanosadchyi.labs.appz_lab02.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {
    @OneToMany(mappedBy = "student")
    private List<Grade> grades;

    @ManyToOne
    private Parent parent;
}
