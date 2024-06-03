package com.midnight.adaem.diningReview.repository;

import com.midnight.adaem.diningReview.model.Reviewer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ReviewerRepository  extends CrudRepository<Reviewer, Long> {

    // does user name exist for given display name
    Boolean existsByDisplayName(String displayName);
    // add user (save does this already)
    // update user (save does this already)
    // get user profile for a display name
    Optional<Reviewer> findByDisplayName(String displayName);

}
