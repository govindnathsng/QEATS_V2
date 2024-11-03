
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        Double servingRadiusInKms = isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

        List<Restaurant> allRestaurants = restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(), 
        getRestaurantsRequest.getLongitude(), currentTime, servingRadiusInKms);
        List<Restaurant> filteredRestaurants = new ArrayList<>();

        for (Restaurant restaurant : allRestaurants) {
          if (getRestaurantsRequest.getSearchFor() == null ||
              restaurant.getAttributes().contains(getRestaurantsRequest.getSearchFor())) {
            filteredRestaurants.add(restaurant);
          }
        }
      
        return new GetRestaurantsResponse(filteredRestaurants);
    //  return null;
  }
  private boolean isPeakHour(LocalTime currentTime) {
    LocalTime peakHourStart1 = LocalTime.of(7, 59);   // 8 AM
    LocalTime peakHourEnd1 = LocalTime.of(10, 1);    // 10 AM
    LocalTime peakHourStart2 = LocalTime.of(12, 59);  // 1 PM
    LocalTime peakHourEnd2 = LocalTime.of(14, 1);    // 2 PM
    LocalTime peakHourStart3 = LocalTime.of(18, 59);  // 7 PM
    LocalTime peakHourEnd3 = LocalTime.of(21, 1);    // 9 PM

    return (currentTime.isAfter(peakHourStart1) && currentTime.isBefore(peakHourEnd1)) ||
        (currentTime.isAfter(peakHourStart2) && currentTime.isBefore(peakHourEnd2)) ||
        (currentTime.isAfter(peakHourStart3) && currentTime.isBefore(peakHourEnd3));
  }
  

  // @Override
  // public GetRestaurantsResponse findAllRestaurantsCloseBy(
  //     GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {


  // }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        Double servingRadiusInKms;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);
    if (((currentTime.isAfter(LocalTime.parse("07:59:59", formatter)))
        && (currentTime.isBefore(LocalTime.parse("10:00:01", formatter))))
        || ((currentTime.isAfter(LocalTime.parse("12:59:59", formatter)))
            && (currentTime.isBefore(LocalTime.parse("14:00:01", formatter))))
        || ((currentTime.isAfter(LocalTime.parse("18:59:59", formatter)))
            && (currentTime.isBefore(LocalTime.parse("21:00:01", formatter))))) {
      servingRadiusInKms = peakHoursServingRadiusInKms;
    } else {
      servingRadiusInKms = normalHoursServingRadiusInKms;
    }

    List<Restaurant> restaurants = new ArrayList<>();
    List<List<Restaurant>> allRestaurants = new ArrayList<>();

    if (getRestaurantsRequest.getSearchFor().length() != 0) {

      allRestaurants.add(new ArrayList<>(restaurantRepositoryService.findRestaurantsByName(
          getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
          getRestaurantsRequest.getSearchFor(), currentTime, servingRadiusInKms)));

      allRestaurants.add(new ArrayList<>(restaurantRepositoryService.findRestaurantsByAttributes(
          getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
          getRestaurantsRequest.getSearchFor(), currentTime, servingRadiusInKms)));

      allRestaurants.add(new ArrayList<>(restaurantRepositoryService.findRestaurantsByItemName(
          getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
          getRestaurantsRequest.getSearchFor(), currentTime, servingRadiusInKms)));

      allRestaurants
          .add(new ArrayList<>(restaurantRepositoryService.findRestaurantsByItemAttributes(
              getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
              getRestaurantsRequest.getSearchFor(), currentTime, servingRadiusInKms)));

      // restaurants = allRestaurants.stream()
      // .flatMap(List::stream)
      // .collect(Collectors.toList());

      for (List<Restaurant> restaurant : allRestaurants) {
        for (Restaurant restaurant2 : restaurant) {
          restaurants.add(restaurant2);
        }
      }

      return new GetRestaurantsResponse(restaurants);

    }
    return new GetRestaurantsResponse(restaurants);
        
  }

  

  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  // Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

        LinkedHashSet<Restaurant> restaurantsHash = new LinkedHashSet<Restaurant>();
        String searchString = getRestaurantsRequest.getSearchFor();
        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        if (searchString != "") {
          Double latitude = getRestaurantsRequest.getLatitude();
          Double longitude = getRestaurantsRequest.getLongitude();
          Double servingRadiusInKms = isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;
    
          CompletableFuture<List<Restaurant>> l1 = restaurantRepositoryService.findRestaurantsByNameMT(latitude, longitude,
              searchString, currentTime, servingRadiusInKms);
    
          CompletableFuture<List<Restaurant>> l2 = restaurantRepositoryService.findRestaurantsByAttributesMT(latitude,
              longitude, searchString, currentTime, servingRadiusInKms);
    
          CompletableFuture<List<Restaurant>> l3 = restaurantRepositoryService.findRestaurantsByItemNameMT(latitude,
              longitude, searchString, currentTime, servingRadiusInKms);
    
          CompletableFuture<List<Restaurant>> l4 = restaurantRepositoryService.findRestaurantsByItemAttributesMT(latitude,
              longitude, searchString, currentTime, servingRadiusInKms);
    
          CompletableFuture.allOf(l1, l2, l3, l4).join();
      
          restaurantsHash.addAll((Collection<? extends Restaurant>) l1);
          restaurantsHash.addAll((Collection<? extends Restaurant>) l2);
          restaurantsHash.addAll((Collection<? extends Restaurant>) l3);
          restaurantsHash.addAll((Collection<? extends Restaurant>) l4);
    
          restaurants = new ArrayList<Restaurant>(restaurantsHash);
          
        }
          return new GetRestaurantsResponse(restaurants);
  }
}

