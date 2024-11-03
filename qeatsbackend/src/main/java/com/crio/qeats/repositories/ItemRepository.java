
package com.crio.qeats.repositories;

import java.util.List;
import java.util.Optional;
import com.crio.qeats.models.ItemEntity;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface ItemRepository extends MongoRepository<ItemEntity, String> {
    // @Query("{'itemName': {$regex: ?0, $options: 'i'}}")
    // List<ItemEntity> findItemsByItemName(String itemName);
    @Query("{name : ?0}")
    Optional<ItemEntity> findRestaurantsByItemName(String name);
    @Query("{attributes: { $in: ?0 } })")
    List<ItemEntity> findRestaurantsByItemAtt(String attributes);
}

