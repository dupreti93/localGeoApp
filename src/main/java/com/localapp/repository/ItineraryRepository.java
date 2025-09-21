package com.localapp.repository;

import com.localapp.model.Itinerary;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ItineraryRepository {

    private final DynamoDbTable<Itinerary> itineraryTable;

    @Autowired
    public ItineraryRepository(DynamoDbEnhancedClient enhancedClient) {
        this.itineraryTable = enhancedClient.table("AIItineraries", TableSchema.fromBean(Itinerary.class));
    }

    public void save(Itinerary itinerary) {
        itineraryTable.putItem(itinerary);
    }

    public Itinerary findById(String userId, String itineraryId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(itineraryId)
                .build();
        return itineraryTable.getItem(key);
    }

    public List<Itinerary> findByUserId(String userId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build()
        );

        return itineraryTable.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public void delete(String userId, String itineraryId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(itineraryId)
                .build();
        itineraryTable.deleteItem(key);
    }
}
