/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.ItemEntity;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.ItemRepository;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
    private RestaurantRepository restaurantRepository;

  @Autowired
    private ItemRepository itemRepository;
  
  @Autowired
    private MenuRepository menuRepository;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
  
  // List<RestaurantEntity> allRestaurants = restaurantRepository.findAll();

  // List<Restaurant> filteredRestaurants = allRestaurants.stream()
  //         .filter(restaurantEntity -> isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms))
  //         .map(this::convertToRestaurant)
  //         .collect(Collectors.toList());

  // return filteredRestaurants;
// }
  if (redisConfiguration.isCacheAvailable()) {
    return findAllRestaurantsCache(latitude, longitude, currentTime, servingRadiusInKms);
  } else { 
    return findAllRestaurantsMongo(latitude, longitude, currentTime, servingRadiusInKms);
  }
}


public List<Restaurant> findAllRestaurantsMongo(Double latitude,
    Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
  System.out.println("------in MONGO function -------");

  ObjectMapper objectMapper = new ObjectMapper();

  List<RestaurantEntity> allRestaurants = restaurantRepository.findAll();

  List<Restaurant> filteredRestaurants = allRestaurants.stream()
          .filter(restaurantEntity -> isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms))
          .map(this::convertToRestaurant)
          .collect(Collectors.toList());

  String restaurantDbString = "";
  redisConfiguration.initCache();
  try {
    restaurantDbString = objectMapper.writeValueAsString(filteredRestaurants);
  } catch (IOException e) {
    e.printStackTrace();
  }
  System.out.print(restaurantDbString);

  GeoLocation geoLocation = new GeoLocation(latitude,longitude);
  GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
      geoLocation.getLongitude(),7);
  Jedis jedis = redisConfiguration.getJedisPool().getResource();
  jedis.set(geoHash.toBase32(),restaurantDbString);
  return filteredRestaurants;
}

private List<Restaurant> findAllRestaurantsCache(Double latitude,
    Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
  System.out.println("------in JEDIS function -----------");
  List<Restaurant> restaurants = new ArrayList<Restaurant>();
  GeoLocation geoLocation = new GeoLocation(latitude,longitude);
  GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
      geoLocation.getLongitude(), 7);

  Jedis jedis = redisConfiguration.getJedisPool().getResource();

  if (!jedis.exists(geoHash.toBase32())) {
    return findAllRestaurantsMongo(latitude, longitude, currentTime, servingRadiusInKms);
  }

  String restaurantString = "";

  ObjectMapper objectMapper = new ObjectMapper();

  try {
    restaurantString = jedis.get(geoHash.toBase32());
    restaurants = objectMapper.readValue(restaurantString,
        new TypeReference<List<Restaurant>>() {});
  } catch (IOException e) {
    e.printStackTrace();
  }
  System.out.print(restaurantString);

  return restaurants;
}


private Restaurant convertToRestaurant(RestaurantEntity restaurantEntity) {
  ModelMapper modelMapper = modelMapperProvider.get();
  return modelMapper.map(restaurantEntity, Restaurant.class);
}




  // public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
  //     Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

  //   List<Restaurant> restaurants = null;




  //   return restaurants;
  // }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

    BasicQuery query = new BasicQuery("{name: {$regex: /" + searchString + "/i}}");
    List<RestaurantEntity> restaurants = mongoTemplate
        .find(query, RestaurantEntity.class, "restaurants");
    List<Restaurant> restaurantList = new ArrayList<Restaurant>();

    for (RestaurantEntity restaurant : restaurants) {

      if (isRestaurantCloseByAndOpen(restaurant, currentTime,
          latitude, longitude, servingRadiusInKms)) {
        Restaurant r = new Restaurant(
        restaurant.getRestaurantId(),
        restaurant.getName(),
        restaurant.getCity(),
        restaurant.getImageUrl(),
        restaurant.getLatitude(),
        restaurant.getLongitude(),
        restaurant.getOpensAt(),
        restaurant.getClosesAt(),
        restaurant.getAttributes()
        );
        

        restaurantList.add(r);

      }

    }
    return restaurantList;
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
      

  BasicQuery query = new BasicQuery("{attributes: {$regex: /" + searchString + "/i}}");

        List<RestaurantEntity> restaurants = mongoTemplate
            .find(query, RestaurantEntity.class, "restaurants");

        List<Restaurant> restaurantList = new ArrayList<Restaurant>();
    
        for (RestaurantEntity restaurant : restaurants) {
    
          if (isRestaurantCloseByAndOpen(restaurant, currentTime,
              latitude, longitude, servingRadiusInKms)) {
            Restaurant r = new Restaurant(
              
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getCity(),
                restaurant.getImageUrl(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getOpensAt(),
                restaurant.getClosesAt(),
                restaurant.getAttributes()
            );
            
    
            restaurantList.add(r);
    
          }
    
        }
        return restaurantList;
  }




  @Override
  public List<Restaurant> findRestaurantsByItemName(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

    BasicQuery query = new BasicQuery("{'items.name': {$regex: /" + searchString + "/i}}");

    List<MenuEntity> menus = mongoTemplate.find(query, MenuEntity.class, "menus");

    List<RestaurantEntity> restaurants = new ArrayList<>();
    for (MenuEntity menu : menus) {
      String restaurantId = menu.getRestaurantId();
      BasicQuery restaurantQuery = new BasicQuery("{restaurantId:" + restaurantId + "}");
      restaurants.add(mongoTemplate
          .findOne(restaurantQuery, RestaurantEntity.class, "restaurants"));
    }
    List<Restaurant> restaurantList = new ArrayList<Restaurant>();

    for (RestaurantEntity restaurant : restaurants) {

      if (isRestaurantCloseByAndOpen(restaurant, currentTime,
          latitude, longitude, servingRadiusInKms)) {
        Restaurant r = new Restaurant(
          restaurant.getRestaurantId(),
            restaurant.getName(),
            restaurant.getCity(),
            restaurant.getImageUrl(),
            restaurant.getLatitude(),
            restaurant.getLongitude(),
            restaurant.getOpensAt(),
            restaurant.getClosesAt(),
            restaurant.getAttributes()
        );
        

        restaurantList.add(r);

      }

    }
    return restaurantList;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        
        BasicQuery query = new BasicQuery("{'items.attributes': {$regex: /" + searchString + "/i}}");
        List<MenuEntity> menus = mongoTemplate.find(query, MenuEntity.class, "menus");
        List<RestaurantEntity> restaurants = new ArrayList<>();
        for (MenuEntity menu : menus) {
          String restaurantId = menu.getRestaurantId();
          BasicQuery restaurantQuery = new BasicQuery("{restaurantId:" + restaurantId + "}");
          restaurants.add(mongoTemplate
              .findOne(restaurantQuery, RestaurantEntity.class, "restaurants"));
        }
        List<Restaurant> restaurantList = new ArrayList<Restaurant>();
    
        for (RestaurantEntity restaurant : restaurants) {
    
          if (isRestaurantCloseByAndOpen(restaurant, currentTime,
              latitude, longitude, servingRadiusInKms)) {
            Restaurant r = new Restaurant(
              restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getCity(),
                restaurant.getImageUrl(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getOpensAt(),
                restaurant.getClosesAt(),
                restaurant.getAttributes()
            );
    
            restaurantList.add(r);
    
          }
    
        }
        return restaurantList;
  }

  
 


  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }

  @Async
  @Override
  public CompletableFuture<List<Restaurant>> findRestaurantsByNameMT(Double latitude, Double longitude, String searchString,
      LocalTime currentTime, Double servingRadiusInKms) {

    ModelMapper modelMapper = modelMapperProvider.get();
    List<RestaurantEntity> restaurantEntities = (List<RestaurantEntity>) restaurantRepository
        .findRestaurantsByNameExact(searchString).stream().collect(Collectors.toList()).get(0);

    List<Restaurant> restaurants = new ArrayList<Restaurant>();

    for (RestaurantEntity restaurantEntity : restaurantEntities) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms)) {
        restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
      }
    }

    return CompletableFuture.completedFuture(restaurants);
  }

  @Async
  @Override
  public CompletableFuture<List<Restaurant>> findRestaurantsByAttributesMT(Double latitude, Double longitude, String searchString,
      LocalTime currentTime, Double servingRadiusInKms) {

    ModelMapper modelMapper = modelMapperProvider.get();
    List<RestaurantEntity> restaurantEntities = (List<RestaurantEntity>) restaurantRepository
        .findRestaurantsByAttributesExact(searchString).stream().collect(Collectors.toList()).get(0);

    List<Restaurant> restaurants = new ArrayList<Restaurant>();

    for (RestaurantEntity restaurantEntity : restaurantEntities) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms)) {
        restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
      }
    }

    return CompletableFuture.completedFuture(restaurants);
  }

  @Async
  @Override
  public CompletableFuture<List<Restaurant>> findRestaurantsByItemNameMT(Double latitude, Double longitude, String searchString,
      LocalTime currentTime, Double servingRadiusInKms) {

    ModelMapper modelMapper = modelMapperProvider.get();
    List<ItemEntity> itemEntity = itemRepository.findRestaurantsByItemName(searchString).stream()
        .collect(Collectors.toList());

    List<String> itemsStr = new ArrayList<String>();
    for (ItemEntity Ie : itemEntity) {
      itemsStr.add(Ie.getId());
    }

    List<MenuEntity> menuEntity = menuRepository.findMenusByItemsItemIdIn(
                                              itemsStr).stream().collect(Collectors.toList()).get(0);

        List<String> restIds = new ArrayList<String>();

        for (MenuEntity Me: menuEntity){
          restIds.add(Me.getRestaurantId());
      }


      List<RestaurantEntity> restaurantEntities =  restaurantRepository.findRestaurantsByid(restIds);


        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        
        for (RestaurantEntity restaurantEntity : restaurantEntities) {
          if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms)) {
            restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
          }
        }
        
  
  
        return CompletableFuture.completedFuture(restaurants);
  }

  @Async
  @Override
  public CompletableFuture<List<Restaurant>> findRestaurantsByItemAttributesMT(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

      ModelMapper modelMapper = modelMapperProvider.get();
      List<ItemEntity> lisItems =  itemRepository.findRestaurantsByItemAtt(searchString);
      List<String> itemsStr = new ArrayList<String>();
      for (ItemEntity Ie: lisItems){
          itemsStr.add(Ie.getId());
      }
    
      List<MenuEntity> menuEntity = menuRepository.findMenusByItemsItemIdIn(
                                      itemsStr).stream().collect(Collectors.toList()).get(0);

      List<String> restIds = new ArrayList<String>();

      for (MenuEntity Me: menuEntity){
        restIds.add(Me.getRestaurantId());
      }

    List<RestaurantEntity> restaurantEntities =  restaurantRepository.findRestaurantsByid(restIds);

      List<Restaurant> restaurants = new ArrayList<Restaurant>();
      
      for (RestaurantEntity restaurantEntity : restaurantEntities) {
        if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms)) {
          restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
        }
      }

      return CompletableFuture.completedFuture(restaurants);

  }



}

