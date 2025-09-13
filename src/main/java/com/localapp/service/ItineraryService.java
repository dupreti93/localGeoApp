package com.localapp.service;

import com.localapp.model.Itinerary;
import com.localapp.repository.ItineraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItineraryService {
    private final ItineraryRepository itineraryRepository;

    @Autowired
    public ItineraryService(ItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    public List<Itinerary> getUserItinerary(String userId, String city, String date) {
        return itineraryRepository.findByUserCityDate(userId, city, date);
    }

    public void saveOrUpdateItinerary(Itinerary itinerary) {
        itineraryRepository.save(itinerary);
    }
}

