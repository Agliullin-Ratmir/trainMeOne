package com.example.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Position {
    private String positionTitle;
    private int durationInMonths;
    private String companyName;
}
