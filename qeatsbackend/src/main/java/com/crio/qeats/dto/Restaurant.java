
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;


// TODO: CRIO_TASK_MODULE_SERIALIZATION
//  Implement Restaurant class.
// Complete the class such that it produces the following JSON during serialization.
// {
//  "restaurantId": "10",
//  "name": "A2B",
//  "city": "Hsr Layout",
//  "imageUrl": "www.google.com",
//  "latitude": 20.027,
//  "longitude": 30.0,
//  "opensAt": "18:00",
//  "closesAt": "23:00",
//  "attributes": [
//    "Tamil",
//    "South Indian"
//  ]
// }
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// @Getter
// @Setter

public class Restaurant {

    
  
    
    private String restaurantId;
    private String name;
    private String city;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private String opensAt;
    private String closesAt;
    private List<String> attributes = new ArrayList<String>();
    // private List<Item> items = new ArrayList<>();
    public Restaurant() {}
    
    public Restaurant( String restaurantId, String name, String city, String imageUrl,
            double latitude, double longitude, String opensAt, String closesAt,
            List<String> attributes) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.city = city;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.attributes = attributes;
        
    }
    // public Restaurant(String restaurantId, String name, String city, String imageUrl,
    //         double latitude, double longitude, String opensAt, String closesAt,
    //         List<String> attributes, List<Item> items) {
    //     this.restaurantId = restaurantId;
    //     this.name = name;
    //     this.city = city;
    //     this.imageUrl = imageUrl;
    //     this.latitude = latitude;
    //     this.longitude = longitude;
    //     this.opensAt = opensAt;
    //     this.closesAt = closesAt;
    //     this.attributes = attributes;
    //     this.items = items;
    // }

    // public List<Item> getItems() {
    //     return items;
    // }

    // public void setItems(List<Item> items) {
    //     this.items = items;
    // }

    public String getRestaurantId() {
        return restaurantId;
    }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getOpensAt() {
        return opensAt;
    }
    public void setOpensAt(String opensAt) {
        this.opensAt = opensAt;
    }
    public String getClosesAt() {
        return closesAt;
    }
    public void setClosesAt(String closesAt) {
        this.closesAt = closesAt;
    }
    public List<String> getAttributes() {
        return attributes;
    }
    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

}

