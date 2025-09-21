import React, { useState } from 'react';
import '../../styles/ItineraryView.css';

const ItineraryView = ({ itinerary }) => {
  const [activeDay, setActiveDay] = useState(0);

  // This is a placeholder component - in a real implementation,
  // you would receive an actual itinerary from your backend

  // Mock data for demonstration purposes
  const mockItinerary = {
    title: "Your NYC Adventure",
    city: "New York",
    startDate: new Date().toLocaleDateString(),
    endDate: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toLocaleDateString(),
    description: "A carefully crafted itinerary based on your selected events and preferences.",
    dayPlans: [
      {
        day: 1,
        date: new Date().toLocaleDateString(),
        activities: [
          {
            time: "9:00 AM",
            title: "Breakfast at Starbucks",
            type: "food",
            location: "Times Square Starbucks",
            duration: "30 mins"
          },
          {
            time: "10:00 AM",
            title: "Visit the Metropolitan Museum of Art",
            type: "attraction",
            location: "The Met",
            duration: "2 hours"
          },
          {
            time: "12:30 PM",
            title: "Lunch at Shake Shack",
            type: "food",
            location: "Madison Square Park",
            duration: "1 hour"
          },
          {
            time: "2:00 PM",
            eventId: "event1",
            title: "Broadway Show: Hamilton",
            type: "event",
            location: "Richard Rodgers Theatre",
            duration: "3 hours"
          },
          {
            time: "6:00 PM",
            title: "Dinner at Carmine's",
            type: "food",
            location: "Times Square",
            duration: "1.5 hours"
          }
        ],
        notes: "Take the subway from the hotel to save time. The Metropolitan Museum closes at 5 PM."
      },
      {
        day: 2,
        date: new Date(Date.now() + 24 * 60 * 60 * 1000).toLocaleDateString(),
        activities: [
          {
            time: "9:30 AM",
            title: "Breakfast at local café",
            type: "food",
            location: "Chelsea Market",
            duration: "45 mins"
          },
          {
            time: "10:30 AM",
            title: "Walk the High Line",
            type: "attraction",
            location: "The High Line",
            duration: "1.5 hours"
          },
          {
            time: "12:30 PM",
            eventId: "event2",
            title: "Food Tour in Greenwich Village",
            type: "event",
            location: "Greenwich Village",
            duration: "3 hours"
          }
        ],
        notes: "Wear comfortable shoes for the High Line walk."
      }
    ],
    notes: "All restaurant reservations have been made. Remember to bring your tickets for the Broadway show."
  };

  // Use the actual itinerary if provided, otherwise use the mock data
  const displayItinerary = itinerary || mockItinerary;

  // Function to get icon based on activity type
  const getActivityIcon = (type) => {
    switch(type) {
      case 'food':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 8.25v-1.5m0 1.5c-1.355 0-2.697.056-4.024.166C6.845 8.51 6 9.473 6 10.608v2.513m6-4.87c1.355 0 2.697.055 4.024.165C17.155 8.51 18 9.473 18 10.608v2.513m-3-4.87v-1.5m-6 1.5v-1.5m12 9.75l-1.5.75a3.354 3.354 0 01-3 0 3.354 3.354 0 00-3 0 3.354 3.354 0 01-3 0 3.354 3.354 0 00-3 0 3.354 3.354 0 01-3 0L3 16.5m15-3.38a48.474 48.474 0 00-6-.37c-2.032 0-4.034.125-6 .37m12 0c.39.049.777.102 1.163.16 1.07.16 1.837 1.094 1.837 2.175v5.17c0 .62-.504 1.124-1.125 1.124H4.125A1.125 1.125 0 013 20.625v-5.17c0-1.08.768-2.014 1.837-2.174A47.78 47.78 0 016 13.12M12.265 3.11a.375.375 0 11-.53 0L12 2.845l.265.265zm-3 0a.375.375 0 11-.53 0L9 2.845l.265.265zm6 0a.375.375 0 11-.53 0L15 2.845l.265.265z" />
          </svg>
        );
      case 'event':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
          </svg>
        );
      case 'attraction':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
          </svg>
        );
      default:
        return (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498l4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 00-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0z" />
          </svg>
        );
    }
  };

  return (
    <div className="itinerary-container">
      <div className="itinerary-header">
        <h2 className="itinerary-title">{displayItinerary.title}</h2>
        <div className="itinerary-meta">
          <span className="itinerary-city">{displayItinerary.city}</span>
          <span className="itinerary-dates">
            {displayItinerary.startDate} - {displayItinerary.endDate}
          </span>
        </div>
      </div>

      {displayItinerary.description && (
        <div className="itinerary-description">
          {displayItinerary.description}
        </div>
      )}

      <div className="itinerary-days-tabs">
        {displayItinerary.dayPlans.map((day, index) => (
          <button
            key={index}
            onClick={() => setActiveDay(index)}
            className={`itinerary-day-tab ${activeDay === index ? 'active' : ''}`}
          >
            Day {day.day}
          </button>
        ))}
      </div>

      <div className="itinerary-day-content">
        <div className="itinerary-day-date">
          {displayItinerary.dayPlans[activeDay].date}
        </div>

        <div className="itinerary-activities">
          {displayItinerary.dayPlans[activeDay].activities.map((activity, idx) => (
            <div key={idx} className="itinerary-activity">
              <div className="itinerary-activity-time">
                {activity.time}
              </div>
              <div className="itinerary-activity-connector">
                <div className="itinerary-activity-line"></div>
                <div className="itinerary-activity-dot"></div>
              </div>
              <div className="itinerary-activity-content">
                <div className="itinerary-activity-header">
                  <div className="itinerary-activity-type-icon">
                    {getActivityIcon(activity.type)}
                  </div>
                  <h3 className="itinerary-activity-title">
                    {activity.title}
                  </h3>
                </div>
                <div className="itinerary-activity-details">
                  <div className="itinerary-activity-location">{activity.location}</div>
                  <div className="itinerary-activity-duration">{activity.duration}</div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {displayItinerary.dayPlans[activeDay].notes && (
          <div className="itinerary-day-notes">
            <h4>Notes</h4>
            <p>{displayItinerary.dayPlans[activeDay].notes}</p>
          </div>
        )}
      </div>

      {displayItinerary.notes && (
        <div className="itinerary-general-notes">
          <h4>General Notes</h4>
          <p>{displayItinerary.notes}</p>
        </div>
      )}

      <div className="itinerary-actions">
        <button className="itinerary-action-button">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6.72 13.829c-.24.03-.48.062-.72.096m.72-.096a42.415 42.415 0 0110.56 0m-10.56 0L6.34 18m10.94-4.171c.24.03.48.062.72.096m-.72-.096L17.66 18m0 0l.229 2.523a1.125 1.125 0 01-1.12 1.227H7.231c-.662 0-1.18-.568-1.12-1.227L6.34 18m11.318 0h1.091A2.25 2.25 0 0021 15.75V9.456c0-1.081-.768-2.015-1.837-2.175a48.055 48.055 0 00-1.913-.247M6.34 18H5.25A2.25 2.25 0 013 15.75V9.456c0-1.081.768-2.015 1.837-2.175a48.041 48.041 0 011.913-.247m10.5 0a48.536 48.536 0 00-10.5 0m10.5 0V3.375c0-.621-.504-1.125-1.125-1.125h-8.25c-.621 0-1.125.504-1.125 1.125v3.659M18 10.5h.008v.008H18V10.5zm-3 0h.008v.008H15V10.5z" />
          </svg>
          Print Itinerary
        </button>
        <button className="itinerary-action-button">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
          </svg>
          Edit Itinerary
        </button>
        <button className="itinerary-action-button">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 8.25H7.5a2.25 2.25 0 00-2.25 2.25v9a2.25 2.25 0 002.25 2.25h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25H15m0-3l-3-3m0 0l-3 3m3-3V15" />
          </svg>
          Share Itinerary
        </button>
      </div>
    </div>
  );
};

export default ItineraryView;
