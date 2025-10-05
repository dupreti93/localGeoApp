import axios from 'axios';
import { API_BASE_URL } from '../common/Constants';

class ItineraryService {
  // Generate AI itinerary from selected events
  static async generateItinerary(selectedEvents, token) {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/itinerary/generate`,
        selectedEvents,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      return response.data;
    } catch (error) {
      console.error('Error generating itinerary:', error);
      throw new Error(error.response?.data?.message || 'Failed to generate itinerary');
    }
  }

  // Get all AI itineraries for the user
  static async getUserItineraries(token) {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/itinerary/ai`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching user itineraries:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch itineraries');
    }
  }

  // Get a specific itinerary by ID
  static async getItineraryById(itineraryId, token) {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/itinerary/ai/${itineraryId}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching itinerary by ID:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch itinerary');
    }
  }

  // Delete an itinerary
  static async deleteItinerary(itineraryId, token) {
    try {
      await axios.delete(
        `${API_BASE_URL}/itinerary/ai/${itineraryId}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
    } catch (error) {
      console.error('Error deleting itinerary:', error);
      throw new Error(error.response?.data?.message || 'Failed to delete itinerary');
    }
  }

  // Format events for API call
  static formatEventsForAPI(savedEvents) {
    return savedEvents.map(event => ({
      id: event.id,
      name: event.name,
      venue: event._embedded?.venues?.[0]?.name || event.venue || 'Unknown Venue',
      startDate: event.dates?.start?.dateTime || event.startDate
    }));
  }
}

export default ItineraryService;
