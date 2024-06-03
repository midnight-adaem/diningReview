package com.midnight.adaem.diningReview.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue
    private long id;

    @NonNull
    String name;

    private String city;
    private String state;
    private String zipcode;


    private Float score;
    private Float peanutScore;
    private Float eggScore;
    private Float dairyScore;

}
