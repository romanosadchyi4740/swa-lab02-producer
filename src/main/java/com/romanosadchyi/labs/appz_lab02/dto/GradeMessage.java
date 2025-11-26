package com.romanosadchyi.labs.appz_lab02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeMessage {
    private GradeDto grade;
    private String parentEmail;
    private Boolean fromRest = false;
}
