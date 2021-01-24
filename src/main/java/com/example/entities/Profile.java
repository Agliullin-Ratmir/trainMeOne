package com.example.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class Profile implements Serializable {
    private String id;
    private String firstName;//+
    private String lastName;//+
    private byte[] photo;
    private double price;
    private String description;
    private String link;
    private String title;//+
    private String company;//+
    private List<Position> positions = new ArrayList<>();
    private String area;
    private String skills;//next
    private int experience;//after skills and experience
    private String certificates;//next
    private double rating = 0;
}