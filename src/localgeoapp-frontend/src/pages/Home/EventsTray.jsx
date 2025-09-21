import React from 'react';
import '../../styles/EventsTray.css';

const EventsTray = ({ savedEvents, loading, formatDate }) => {
  // Reverse the events array to show newest events first
  const eventsToDisplay = savedEvents ? [...savedEvents].reverse() : [];

  return (
    <div className="events-tray">
      <div className="events-container">
        <div className="events-header">
          <div className="events-title-container">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="events-icon">
              <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5" />
            </svg>
            My Events
          </div>

          {/* Subtle dots indicator for scrollability */}
          {eventsToDisplay.length > 2 && (
            <div className="events-scroll-dots">
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
            </div>
          )}
        </div>

        {loading ? (
          <div className="events-loading">
            <div className="events-spinner"></div>
          </div>
        ) : eventsToDisplay.length > 0 ? (
          <div className="events-horizontal-list">
            {eventsToDisplay.map((event) => (
              <div
                key={event.id || event.eventId}
                className="event-card-horizontal"
              >
                <div className="event-image-container-horizontal">
                  <img
                    src={event.image}
                    alt={event.name}
                    className="event-image"
                  />
                </div>
                <div className="event-content-horizontal">
                  <h4 className="event-title">{event.name}</h4>
                  <p className="event-date">
                    {formatDate ? formatDate(event.startDate) :
                      new Date(event.startDate).toLocaleString(undefined, {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })
                    }
                  </p>
                </div>
              </div>
            ))}

            {/* Visual indication at the end that there's more to scroll */}
            {eventsToDisplay.length > 2 && (
              <div className="scroll-indicator-end"></div>
            )}
          </div>
        ) : (
          <div className="events-empty">
            <p>No saved events yet.</p>
            <p className="events-empty-subtext">
              Add events to see them here.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default EventsTray;
