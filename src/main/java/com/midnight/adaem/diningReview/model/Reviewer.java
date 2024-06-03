package com.midnight.adaem.diningReview.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
public class Reviewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @Column(unique = true)
    private String displayName;

    private String city;
    private String state;
    private String zipcode;

    @NonNull
    private Boolean peanutAllergy;
    @NonNull
    private Boolean eggAllergy;
    @NonNull
    private Boolean dairyAllergy;

}
