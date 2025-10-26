import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import TravelMap from './MyTravel/TravelMap';
import '../styles/MyTravel.css';

const MyTravel = () => {
  const { token, user } = useContext(AuthContext);
  const [savedEvents, setSavedEvents] = useState([]);
  const [loading, setLoading] = useState(false);

  // Load saved events from localStorage
  useEffect(() => {
    try {
      const storedEvents = localStorage.getItem('savedEvents');
      if (storedEvents) {
        setSavedEvents(JSON.parse(storedEvents));
      }
    } catch (error) {
      console.error('Error loading saved events:', error);
    }
  }, []);

  if (!user) {
    return (
      <div className="my-travel-container">
        <div className="auth-required">
          <h2>Please log in to view your saved events</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="my-travel-container">
      <div className="my-travel-header">
        <h1>My Saved Events</h1>
        <p>View your saved events on the map</p>
      </div>

      <div className="my-travel-content">
        {savedEvents.length > 0 ? (
          <>
            <div className="events-summary">
              <h3>You have {savedEvents.length} saved event{savedEvents.length !== 1 ? 's' : ''}</h3>
            </div>

            <div className="map-container">
              <TravelMap events={savedEvents} />
            </div>

            <div className="events-list">
              <h3>Event Details</h3>
              {savedEvents.map((event, index) => (
                <div key={event.id || index} className="event-item">
                  <div className="event-info">
                    <h4>{event.name}</h4>
                    <p className="event-venue">📍 {event.venue}</p>
                    <p className="event-date">📅 {new Date(event.startDate).toLocaleDateString()}</p>
                    {event.minPrice && (
                      <p className="event-price">💰 ${event.minPrice} - ${event.maxPrice}</p>
                    )}
                  </div>
                  {event.url && (
                    <a
                      href={event.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="ticket-link"
                    >
                      Get Tickets
                    </a>
                  )}
                </div>
              ))}
            </div>
          </>
        ) : (
          <div className="no-events">
            <h3>No saved events yet</h3>
            <p>Start by searching for artists and saving events you're interested in!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyTravel;
