package com.midnight.adaem.diningReview.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class AdminReview {
    private long id;

    @NonNull
    private Boolean approved;
    @NonNull
    private String reviewer;
    @NonNull
    private Long reviewId;

}
