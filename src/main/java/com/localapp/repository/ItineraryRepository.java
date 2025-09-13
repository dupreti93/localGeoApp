package com.localapp.repository;

import com.localapp.model.Itinerary;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ItineraryRepository {
    private final DynamoDbTable<Itinerary> itineraryTable;

    public ItineraryRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.itineraryTable = dynamoDbEnhancedClient.table("Itinerary", TableSchema.fromBean(Itinerary.class));
    }

    public List<Itinerary> findByUserCityDate(String userId, String city, String date) {
        return itineraryTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(userId)))
                .items().stream()
                .filter(i -> city.equalsIgnoreCase(i.getCity()) && date.equals(i.getDate()))
                .collect(Collectors.toList());
    }

    public void save(Itinerary itinerary) {
        itineraryTable.putItem(itinerary);
    }
}

