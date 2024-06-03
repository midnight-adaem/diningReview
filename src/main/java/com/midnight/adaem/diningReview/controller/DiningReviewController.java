package com.midnight.adaem.diningReview.controller;

import com.midnight.adaem.diningReview.enums.ReviewStatus;
import com.midnight.adaem.diningReview.model.DiningReview;
import com.midnight.adaem.diningReview.model.Restaurant;
import com.midnight.adaem.diningReview.model.Reviewer;
import com.midnight.adaem.diningReview.repository.DiningReviewRepository;
import com.midnight.adaem.diningReview.repository.RestaurantRepository;
import com.midnight.adaem.diningReview.repository.ReviewerRepository;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/diningReview")
public class DiningReviewController {
    private final ReviewerRepository reviewerRepository;
    private final DiningReviewRepository diningReviewRepository;
    private final RestaurantRepository restaurantRepository;

    public DiningReviewController (final ReviewerRepository reviewerRepository, final DiningReviewRepository diningReviewRepository, final RestaurantRepository restaurantRepository) {
        this.reviewerRepository=reviewerRepository;
        this.diningReviewRepository=diningReviewRepository;
        this.restaurantRepository=restaurantRepository;
    }

    // ****** Reviewer related calls

    // check to see if display name exists
    @GetMapping("/reviewer/exists/{displayName}")
    public Boolean DisplayNameExists(@PathVariable String displayName) {
        return this.reviewerRepository.existsByDisplayName(displayName);
    }

    // add reviewer
    @PostMapping("/reviewer")
    public Reviewer createNewReviewer(@RequestBody @NonNull Reviewer reviewer) {
        // Check to make sure this reviewer doesn't already exist
        if (!this.reviewerRepository.existsByDisplayName(reviewer.getDisplayName())) {
            Reviewer newReviewer = this.reviewerRepository.save(reviewer);
            return newReviewer;
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Reviewer with display name  " + reviewer.getDisplayName() + " already exists. ");
        }
    }

    // update reviewer profile
    @PutMapping("/reviewer")
    public Reviewer updateReviewer(@RequestBody @NonNull Reviewer reviewer) {
        // try to fetch ID
        Optional<Reviewer> reviewerOptional = this.reviewerRepository.findById(reviewer.getId());
        if (!reviewerOptional.isPresent()) {
            // throw exception
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No reviewer found for ID " + reviewer.getId());
        }

        Reviewer updateReviewer = reviewerOptional.get();
        //update fields

        updateReviewer.setCity(reviewer.getCity());
        updateReviewer.setState(reviewer.getState());
        updateReviewer.setZipcode(reviewer.getZipcode());
        updateReviewer.setEggAllergy(reviewer.getEggAllergy());
        updateReviewer.setDairyAllergy(reviewer.getDairyAllergy());
        updateReviewer.setPeanutAllergy(reviewer.getPeanutAllergy());

        this.reviewerRepository.save(updateReviewer);

        return updateReviewer;
    }

    // fetch profile for a display name (this should have security on it!)
    @GetMapping("/reviewer")
    public Iterable<Reviewer> getAllReviewers() {
        Iterable<Reviewer> reviewers = reviewerRepository.findAll();
        return reviewers;
    }

    @GetMapping("/reviewer/{displayName}")
    public Reviewer getReviewerByDisplayName(@PathVariable String displayName) {
        Optional<Reviewer> reviewerOptional = this.reviewerRepository.findByDisplayName(displayName);
        if (reviewerOptional.isEmpty()) {
            // throw exception
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No reviewer found for name  " + displayName);
        }

        return reviewerOptional.get();
    }


    // ****** Review related calls

    // submit dining review
    @PostMapping("/reviews")
    public DiningReview createNewReview(@RequestBody @NonNull DiningReview review) {
        DiningReview newReviewer;
        if (!this.restaurantRepository.existsById(review.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant with ID " + review.getRestaurantId() + " does not exist in our database. ");
        }

        if (!this.reviewerRepository.existsByDisplayName(review.getSubmittedBy())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer with name " + review.getSubmittedBy() + " does not exist in our database. ");
        }
        newReviewer = this.diningReviewRepository.save(review);
        return newReviewer;
    }

    // list of all pending reviews for admins
    @GetMapping("/admin/pendingReviews")
    public Iterable<DiningReview> getPendingReviews() {
        return diningReviewRepository.findByReviewStatus(ReviewStatus.PENDING);
    }

    // fetch all approved dining reviews for a restaurant
    @GetMapping("/reviews/{restaurantId}")
    public Iterable<DiningReview> getApprovedReviewsByRestaurant(@PathVariable @NonNull Long restaurantId) {
        return diningReviewRepository.findByReviewStatusAndRestaurantId(ReviewStatus.APPROVED, restaurantId);
    }

    // approve or reject review
    @PutMapping("/admin/approve")
    public DiningReview approveReview(@RequestParam @NonNull Long id, @RequestParam @NonNull Boolean approve) {
        Optional<DiningReview> reviewOptional = this.diningReviewRepository.findById(id);
        if (reviewOptional.isEmpty()) {
            // throw exception
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No review found for id " + id);
        }

        DiningReview review = reviewOptional.get();
        DiningReview returnReview;
        if (approve) {
            review.setReviewStatus(ReviewStatus.APPROVED);
            returnReview=this.diningReviewRepository.save(review);
            this.recalculateScores(review);

        } else {
            review.setReviewStatus(ReviewStatus.REJECTED);
            returnReview=this.diningReviewRepository.save(review);
        }

        return returnReview;
    }

    // ****** restaurant related calls
    // recalculate scores for a restaurant when a review is approved
    private Restaurant recalculateScores(DiningReview review) {
        // doing this in SQL would probably be more efficient
        // get all the approved reviews for this restaurant
        Iterable<DiningReview> reviews = diningReviewRepository.findByReviewStatusAndRestaurantId(ReviewStatus.APPROVED, review.getRestaurantId());

        float eggTotal= 0.0F;
        float peanutTotal= 0.0F;
        float dairyTotal= 0.0F;
        long count=0;
        for (DiningReview iReview : reviews) {
            count++;
            eggTotal+=iReview.getEggScore();
            peanutTotal+= iReview.getPeanutScore();;
            dairyTotal+=iReview.getDairyScore();
        }

        // get restaurant
        Restaurant restaurant = getRestaurantById(review.getRestaurantId());

        DecimalFormat df = new DecimalFormat("0.00");
        float newEggScore=(count > 0) ? eggTotal/count : 0;
        float newPeanutScore=(count > 0) ? eggTotal/count : 0;
        float newDairyScore = (count > 0) ? eggTotal/count : 0;
        float newTotalScore = (newDairyScore+newPeanutScore+newEggScore)/3;

        // fill in new scores rounded to 2 decimal places
        restaurant.setEggScore(Float.valueOf(df.format(newEggScore)));
        restaurant.setPeanutScore(Float.valueOf(df.format(newPeanutScore)));
        restaurant.setDairyScore(Float.valueOf(df.format(newDairyScore)));
        restaurant.setScore(Float.valueOf(df.format(newTotalScore)));

        return this.restaurantRepository.save(restaurant);

    }


    // add new restaurant, must not exist for name and zip code
    @PostMapping("/restaurant")
    public Restaurant createNewRestaurant(@RequestBody @NonNull Restaurant restaurant) {
        // Check to make sure this restaurant doesn't already exist
        if (!this.restaurantRepository.existsByNameAndZipcode(restaurant.getName(), restaurant.getZipcode())) {
            return this.restaurantRepository.save(restaurant);
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Restaurant with name  " + restaurant.getName() + " and zip code " + restaurant.getZipcode() + " already exists. ");
        }
    }

    // fetch details of restaurant by ID
    @GetMapping("/restaurant/{restaurantId}")
    public Restaurant getRestaurantById(@PathVariable @NonNull Long restaurantId) {
        Optional<Restaurant> reviewerOptional = this.restaurantRepository.findById(restaurantId);
        if (reviewerOptional.isEmpty()) {
            // throw exception
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No restaurant found for id  " + restaurantId);
        }

        return reviewerOptional.get();
    }

    // fetch restaurants for zip code w/ at least one allergy score sorted in descending order
    @GetMapping("/restaurant/zipcode/{zipcode}")
    public Iterable<Restaurant> getApprovedReviewsByRestaurant(@PathVariable @NonNull String zipcode) {
        return restaurantRepository.findByZipcodeAndAnAllergyScore(zipcode, Sort.by(Sort.Direction.DESC, "name"));
    }

}
