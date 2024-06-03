package com.midnight.adaem.diningReview.repository;

import com.midnight.adaem.diningReview.model.Restaurant;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {

    // add new restaurant (save will do this, no code needed here)
    // restaurant exists for name and zip code
    Boolean existsByNameAndZipcode(String name, String zipCode);

    // fetch details of restaurant by ID (findById)

    // fetch restaurants for zip code w/ at least one allergy score sorted in descending order
    @Query("SELECT r FROM Restaurant r where r.zipcode=?1  and (dairyScore IS NOT NULL or eggScore IS NOT NULL or peanutScore IS NOT NULL)")
    List<Restaurant> findByZipcodeAndAnAllergyScore(String zipCode, Sort sort);

}
