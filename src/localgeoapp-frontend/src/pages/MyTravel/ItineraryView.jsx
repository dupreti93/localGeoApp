import React, { useState, useEffect } from 'react';
import ItineraryService from '../../services/ItineraryService';
import '../../styles/ItineraryView.css';

const ItineraryView = ({ token, user, loading, setLoading, activeSecondaryTab }) => {
  const [generatedItinerary, setGeneratedItinerary] = useState(null);
  const [userItineraries, setUserItineraries] = useState([]);
  const [selectedItineraryId, setSelectedItineraryId] = useState(null);
  const [activeDay, setActiveDay] = useState(0);
  const [error, setError] = useState(null);
  const [fetchLoading, setFetchLoading] = useState(false);

  // Fetch user's existing itineraries
  const fetchUserItineraries = async () => {
    if (!token) return;

    setFetchLoading(true);
    try {
      console.log('🔄 Fetching user itineraries...');
      const itineraries = await ItineraryService.getUserItineraries(token);
      console.log('✅ Fetched itineraries:', itineraries);

      setUserItineraries(itineraries);

      // If user has itineraries, show the most recent one
      if (itineraries.length > 0) {
        const mostRecent = itineraries[0]; // Assuming they're sorted by creation date
        setGeneratedItinerary(mostRecent);
        setSelectedItineraryId(mostRecent.itineraryId);
        console.log('📋 Displaying most recent itinerary:', mostRecent.title);
      } else {
        setGeneratedItinerary(null);
        setSelectedItineraryId(null);
        console.log('❌ No itineraries found');
      }
    } catch (error) {
      console.error('💥 Error fetching user itineraries:', error);
      setError('Failed to load itineraries');
    } finally {
      setFetchLoading(false);
    }
  };

  // Fetch itineraries on component mount and when token changes
  useEffect(() => {
    fetchUserItineraries();
  }, [token]);

  // Refresh itineraries when the itinerary tab becomes active
  useEffect(() => {
    if (activeSecondaryTab === 'itinerary' && token) {
      console.log('🔄 Itinerary tab became active, refreshing itineraries...');
      // Add a small delay to allow for backend processing after navigation
      setTimeout(() => {
        fetchUserItineraries();
      }, 500);
    }
  }, [activeSecondaryTab, token]);

  // Also refresh when the component becomes visible (user switches to MyTravel tab)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden && token && activeSecondaryTab === 'itinerary') {
        console.log('👁️ Tab became visible, refreshing itineraries...');
        fetchUserItineraries();
      }
    };

    // Listen for focus events as well (when user returns to the page)
    const handleFocus = () => {
      if (token && activeSecondaryTab === 'itinerary') {
        console.log('🎯 Window gained focus, refreshing itineraries...');
        fetchUserItineraries();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('focus', handleFocus);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('focus', handleFocus);
    };
  }, [token, activeSecondaryTab]);

  // Handle selecting a different itinerary
  const handleSelectItinerary = async (itineraryId) => {
    try {
      const itinerary = await ItineraryService.getItineraryById(itineraryId, token);
      setGeneratedItinerary(itinerary);
      setSelectedItineraryId(itineraryId);
      setActiveDay(0);
    } catch (error) {
      setError('Failed to load selected itinerary');
      console.error('Error loading itinerary:', error);
    }
  };

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
            <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0021 11.25v7.5" />
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

  // Show loading state if fetching saved events
  if (loading || fetchLoading) {
    return (
      <div className="itinerary-container">
        <div className="loading-state">
          <p>Loading your itineraries...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="itinerary-container">
      {/* Header section with Build Itinerary button */}
      <div className="itinerary-header-section">
        <h3>Your Travel Itinerary</h3>


        {/* Itinerary selector dropdown */}
        {userItineraries.length > 0 && (
          <div className="itinerary-selector">
            <label htmlFor="itinerary-select">Select Itinerary:</label>
            <select
              id="itinerary-select"
              value={selectedItineraryId || ''}
              onChange={(e) => handleSelectItinerary(e.target.value)}
            >
              {userItineraries.map((itinerary) => (
                <option key={itinerary.itineraryId} value={itinerary.itineraryId}>
                  {itinerary.title} - {itinerary.city}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Error message */}
      {error && (
        <div className="error-message">
          <p>{error}</p>
        </div>
      )}

      {/* Generated Itinerary Display */}
      {generatedItinerary ? (
        <>
          <div className="itinerary-header">
            <h2 className="itinerary-title">{generatedItinerary.title}</h2>
            <div className="itinerary-meta">
              <span className="itinerary-city">{generatedItinerary.city}</span>
              <span className="itinerary-dates">
                {generatedItinerary.startDate} - {generatedItinerary.endDate}
              </span>
            </div>
          </div>

          {generatedItinerary.description && (
            <div className="itinerary-description">
              {generatedItinerary.description}
            </div>
          )}

          {/* Day tabs */}
          {generatedItinerary.activities && generatedItinerary.activities.length > 0 && (
            <>
              <div className="itinerary-days-tabs">
                {Array.from(new Set(generatedItinerary.activities.map(a => a.day))).sort().map((dayNum) => (
                  <button
                    key={dayNum}
                    onClick={() => setActiveDay(dayNum - 1)}
                    className={`itinerary-day-tab ${activeDay === dayNum - 1 ? 'active' : ''}`}
                  >
                    Day {dayNum}
                  </button>
                ))}
              </div>

              <div className="itinerary-day-content">
                {(() => {
                  const currentDayActivities = generatedItinerary.activities.filter(a => a.day === activeDay + 1);
                  if (currentDayActivities.length === 0) return <p>No activities for this day</p>;

                  return (
                    <>
                      <div className="itinerary-day-date">
                        {currentDayActivities[0]?.date}
                      </div>

                      <div className="itinerary-activities">
                        {currentDayActivities.map((activity, idx) => (
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
                                {activity.description && (
                                  <div className="itinerary-activity-description">{activity.description}</div>
                                )}
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </>
                  );
                })()}
              </div>
            </>
          )}

          {generatedItinerary.notes && (
            <div className="itinerary-general-notes">
              <h4>General Notes</h4>
              <div className="notes-content">
                {generatedItinerary.notes.split('\n').map((line, idx) => (
                  <p key={idx}>{line}</p>
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <div className="no-itinerary-state">
          <div className="no-itinerary-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498l4.875-2.437c.381-.19.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 00-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0z" />
            </svg>
          </div>
          <h3>No Itinerary Yet</h3>
          <p>Save some events from the Explore tab and click "Build AI Itinerary" to get started!</p>
        </div>
      )}
    </div>
  );
};

export default ItineraryView;
