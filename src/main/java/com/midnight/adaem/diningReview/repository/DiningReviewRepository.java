package com.midnight.adaem.diningReview.repository;

import com.midnight.adaem.diningReview.enums.ReviewStatus;
import com.midnight.adaem.diningReview.model.DiningReview;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DiningReviewRepository  extends CrudRepository<DiningReview, Long> {

    //submit dining review (save will do this)

    // list of all pending reviews for admins
    List<DiningReview> findByReviewStatus(ReviewStatus reviewStatus);
    List<DiningReview> findByReviewStatusAndRestaurantId(ReviewStatus reviewStatus, Long restaurantId);

}
