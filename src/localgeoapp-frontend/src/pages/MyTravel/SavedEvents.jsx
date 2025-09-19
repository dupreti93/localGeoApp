// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\MyTravel\SavedEvents.jsx
import React from 'react';
import '../../styles/SavedEvents.css';

const SavedEvents = ({ loading, savedEvents }) => {
  return (
    <div className="saved-events-container">
      <h3 className="saved-events-header">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="saved-events-icon">
          <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5" />
        </svg>
        Saved Events
      </h3>

      {loading ? (
        <div className="saved-events-loading">
          <div className="saved-events-spinner"></div>
        </div>
      ) : savedEvents.length > 0 ? (
        <div className="saved-events-list">
          {savedEvents.map((event) => (
            <div
              key={event.id}
              className="saved-event-card"
            >
              <div className="saved-event-image-container">
                <img
                  src={event.image}
                  alt={event.name}
                  className="saved-event-image"
                />
              </div>
              <div className="saved-event-content">
                <h4 className="saved-event-title">{event.name}</h4>
                <p className="saved-event-venue">{event.venue}</p>
                <p className="saved-event-date">
                  {new Date(event.startDate).toLocaleString(undefined, {
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="saved-events-empty">
          <p>No saved events yet.</p>
          <p className="saved-events-empty-subtext">
            Save events from the Explore tab to see them here.
          </p>
        </div>
      )}
    </div>
  );
};

export default SavedEvents;
