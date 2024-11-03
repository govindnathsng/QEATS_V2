/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface RestaurantRepository extends MongoRepository<RestaurantEntity, String> {
    List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms);
    // @Query("{'name':{$regex: '^?0$', $options: 'i'}}")
    // Optional<List<RestaurantEntity>> findRestaurantsByNameExact(String searchString);
    @Query("{ 'name' : ?0 }")
  Optional<List<RestaurantEntity>> findRestaurantsByNameExact(String name);

  @Query("{ 'attributes' : { $in : ?0 } }")
  Optional<List<RestaurantEntity>> findRestaurantsByAttributes(List<String> attributes);

  @Query("{ 'items.name' : ?0 }")
  Optional<List<RestaurantEntity>> findRestaurantsByItemName(String itemName);

  @Query("{ 'items.attributes' : { $in : ?0 } }")
  Optional<List<RestaurantEntity>> findRestaurantsByItemAttributes(List<String> itemAttributes);
 

  @Query("{attributes : ?0}")
  Optional<RestaurantEntity> findRestaurantsByAttributesExact(String attributes);

@Query("{restaurantId: { $in: ?0 } })")
  List<RestaurantEntity> findRestaurantsByid(List<String> restaurantId);
}
      


