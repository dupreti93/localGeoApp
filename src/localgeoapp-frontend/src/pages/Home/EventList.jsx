import React from 'react';
import { LocationIcon, CalendarIcon, ArrowLeftIcon, ArrowRightIcon, PlusIcon, ImageIcon } from '../shared/Icons';

const EventList = ({
  events,
  expandedEvent,
  highlightedEvent,
  currentImageIndex,
  onToggleDetails,
  onImageNavigate,
  onEventAction,
  getEventStatus,
  formatDate,
  totalPages,
  currentPage,
  setCurrentPage,
  savedEvents = [], // Prop for saved events
  onAddEvent // New prop for add event handler
}) => {
  // Make sure we have events to display
  const eventsToDisplay = events || [];

  // Default formatDate function if one isn't provided
  const formatEventDate = (date) => {
    if (!formatDate) {
      try {
        return new Date(date).toLocaleString(undefined, {
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (e) {
        return date;
      }
    }
    return formatDate(date);
  };

  // Check if an event is already saved
  const isEventSaved = (eventId) => {
    return savedEvents && savedEvents.some(saved => saved.id === eventId || saved.eventId === eventId);
  };

  return (
    <div className="home-left-column">
      <div className="home-column-header">
        <h2 className="home-column-title">
          <CalendarIcon className="w-5 h-5 mr-2" />
          {eventsToDisplay.length} Events
        </h2>
      </div>

      {eventsToDisplay.map((event) => (
        <div
          key={event.id}
          className={`event-card ${
            expandedEvent === event.id ? 'event-card-expanded' : ''
          } ${
            highlightedEvent === event.id ? 'event-card-highlighted' : ''
          }`}
        >
          <div
            className="cursor-pointer"
            onClick={() => onToggleDetails(event.id)}
          >
            <div className="event-image-container">
              {event.images && event.images.length > 0 ? (
                <img
                  src={event.images[currentImageIndex ? (currentImageIndex[event.id] || 0) : 0] || event.image}
                  alt={event.name}
                  className="event-image"
                />
              ) : (
                <div className="event-image-placeholder">
                  <ImageIcon className="text-gray-400" />
                </div>
              )}

              {event.images && event.images.length > 1 && (
                <>
                  <button
                    onClick={(e) => { e.stopPropagation(); onImageNavigate(event.id, 'prev'); }}
                    className="event-image-nav-button event-image-nav-left"
                  >
                    <ArrowLeftIcon className="w-5 h-5" />
                  </button>
                  <button
                    onClick={(e) => { e.stopPropagation(); onImageNavigate(event.id, 'next'); }}
                    className="event-image-nav-button event-image-nav-right"
                  >
                    <ArrowRightIcon className="w-5 h-5" />
                  </button>
                </>
              )}

              <div className="event-card-caption">
                <h3 className="event-card-title">{event.name}</h3>
                <p className="event-card-subtitle">
                  <LocationIcon className="w-4 h-4 mr-1" />
                  {event.venue}
                </p>
              </div>
            </div>

            <div className="event-card-details">
              <p className="event-card-meta">
                <CalendarIcon className="w-4 h-4 mr-1" />
                {formatEventDate(event.startDate)}
              </p>

              {event.allStartTimes && event.allStartTimes.length > 1 && (
                <div className="mt-2">
                  <p className="text-sm text-gray-600">
                    <span className="font-medium">Multiple times available:</span> {event.allStartTimes.length} showtimes
                  </p>
                </div>
              )}
            </div>
          </div>

          {expandedEvent === event.id && (
            <div className="event-card-expanded-content">
              <a
                href={event.url}
                target="_blank"
                rel="noopener noreferrer"
                className="event-ticket-button"
              >
                Get Tickets
              </a>

              <div className="event-action-container">
                {isEventSaved(event.id) ? (
                  <div className="px-3 py-1 bg-green-100 text-green-700 text-sm rounded text-center w-full">
                    Added to My Events
                  </div>
                ) : (
                  <button
                    onClick={(e) => {e.stopPropagation(); onAddEvent(event.id)}}
                    className="event-action-button w-full"
                  >
                    <PlusIcon className="w-4 h-4 mr-1" />
                    Add to My Events
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      ))}

      {/* Pagination */}
      {totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          setCurrentPage={setCurrentPage}
        />
      )}
    </div>
  );
};

// Pagination component
const Pagination = ({ currentPage, totalPages, setCurrentPage }) => {
  return (
    <div className="pagination-container">
      <div className="pagination-controls">
        <button
          onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
          disabled={currentPage === 1}
          className={`pagination-button ${
            currentPage === 1 ? 'pagination-button-disabled' : ''
          }`}
        >
          <ArrowLeftIcon className="w-5 h-5" />
        </button>

        {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
          // Show pages around current page
          let pageNum;
          if (totalPages <= 5) {
            // If 5 or fewer pages, show all
            pageNum = i + 1;
          } else if (currentPage <= 3) {
            // If near start, show first 5
            pageNum = i + 1;
          } else if (currentPage >= totalPages - 2) {
            // If near end, show last 5
            pageNum = totalPages - 4 + i;
          } else {
            // Otherwise show current and 2 on each side
            pageNum = currentPage - 2 + i;
          }

          return (
            <button
              key={pageNum}
              onClick={() => setCurrentPage(pageNum)}
              className={`pagination-button ${
                currentPage === pageNum ? 'pagination-button-active' : ''
              }`}
            >
              {pageNum}
            </button>
          );
        })}

        <button
          onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
          disabled={currentPage === totalPages}
          className={`pagination-button ${
            currentPage === totalPages ? 'pagination-button-disabled' : ''
          }`}
        >
          <ArrowRightIcon className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
};

export default EventList;
