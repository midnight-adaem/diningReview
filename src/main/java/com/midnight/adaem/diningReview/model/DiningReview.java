package com.midnight.adaem.diningReview.model;

import com.midnight.adaem.diningReview.enums.ReviewStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
public class DiningReview {

    @Id
    @GeneratedValue
    private long id;

    @NonNull
    String submittedBy;
    @NonNull
    private Long restaurantId;
    private Float peanutScore;
    private Float eggScore;
    private Float dairyScore;
    private String commentary;
    private ReviewStatus reviewStatus;
}
