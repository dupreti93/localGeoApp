import { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import Filter from './Home/Filter';
import { AuthContext } from '../context/AuthContext';
import LoginModal from './shared/LoginModal';
import { API_BASE_URL } from '../common/Constants';
import EventList from './Home/EventList';
import EventMap, { MapUpdater } from './Home/EventMap';
import SearchHeader from './Home/SearchHeader';
import { LoadingState, ErrorState, EmptyState, SuccessToast } from './shared/StateComponents';
import { LocationIcon, CalendarIcon, SearchIcon, CheckIcon, ArrowRightIcon, ArrowLeftIcon } from './shared/Icons'; // Added SearchIcon, CheckIcon, and ArrowRightIcon
import '../styles/Home.css';

const DEFAULT_CENTER = [40.74, -73.98]; // NYC default

const Home = () => {
  // State variables
  const { token, user } = useContext(AuthContext);
  const [step, setStep] = useState(1);
  const [city, setCity] = useState('');
  const [date, setDate] = useState('');
  const [searchCity, setSearchCity] = useState('');
  const [searchDate, setSearchDate] = useState('');
  const [events, setEvents] = useState([]);
  const [displayedEvents, setDisplayedEvents] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [itinerary, setItinerary] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [mapCenter, setMapCenter] = useState(DEFAULT_CENTER);
  const [showCityDropdown, setShowCityDropdown] = useState(false);
  const [filteredCities, setFilteredCities] = useState([]);
  const [expandedEvent, setExpandedEvent] = useState(null);
  const [highlightedEvent, setHighlightedEvent] = useState(null);
  const [currentImageIndex, setCurrentImageIndex] = useState({});
  const [artistFilter, setArtistFilter] = useState('');
  const [availableArtists, setAvailableArtists] = useState([]);

  const EVENTS_PER_PAGE = 15;
  const POPULAR_CITIES = [
    'New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix', 'Philadelphia',
    'San Antonio', 'San Diego', 'Dallas', 'San Jose', 'Austin', 'Jacksonville',
    'Fort Worth', 'Columbus', 'Charlotte', 'San Francisco', 'Indianapolis',
    'Seattle', 'Denver', 'Boston', 'Las Vegas', 'Nashville', 'Miami',
    'Atlanta', 'Washington', 'London', 'Paris', 'Berlin', 'Tokyo', 'Sydney'
  ];

  // Calculate totalPages
  const totalPages = Math.ceil(events.length / EVENTS_PER_PAGE);

  // Data fetching and utility functions
  const getCityCoordinates = async (cityName) => {
    try {
      const response = await axios.get(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(cityName)}&limit=1`
      );

      if (response.data && response.data.length > 0) {
        const { lat, lon } = response.data[0];
        return [parseFloat(lat), parseFloat(lon)];
      }

      return DEFAULT_CENTER;
    } catch (error) {
      console.error('Error geocoding city:', error);
      return DEFAULT_CENTER;
    }
  };

  // Fetch events when search parameters change
  useEffect(() => {
    if (!searchCity || !searchDate || step !== 3) return;

    const fetchEventsAndUpdateMap = async () => {
      // Check if we have cached events for this city/date combination
      const cacheKey = `events_${searchCity}_${searchDate}`;
      const cachedEventsData = localStorage.getItem(cacheKey);

      if (cachedEventsData) {
        try {
          // Use cached events if available
          const cachedData = JSON.parse(cachedEventsData);
          console.log('Using cached events data');

          setEvents(cachedData.events);
          setDisplayedEvents(cachedData.events.slice(0, EVENTS_PER_PAGE));
          setMapCenter(cachedData.mapCenter || DEFAULT_CENTER);
          setLoading(false);
          return;
        } catch (error) {
          console.error('Error parsing cached events:', error);
          // Continue with fetch if cache parsing fails
        }
      }

      // If no cache or cache error, proceed with fetch
      setLoading(true);
      setError(null);
      setCurrentPage(1);

      try {
        // Get city coordinates and update map
        const coordinates = await getCityCoordinates(searchCity);
        setMapCenter(coordinates);

        // Fetch events
        const response = await axios.get(
          `${API_BASE_URL}/events?city=${encodeURIComponent(searchCity)}&date=${searchDate}`
        );

        // Group events by venue+name, but preserve all available times
        const eventGroups = {};

        // First, group events by venue+name
        response.data.forEach(event => {
          const groupKey = `${event.venue}|${event.name}`;
          if (!eventGroups[groupKey]) {
            // Create a new group with this event as the base
            eventGroups[groupKey] = {
              ...event,
              allStartTimes: [event.startDate],
              originalStartDate: event.startDate
            };
          } else {
            // Add this time to the existing group's times array
            eventGroups[groupKey].allStartTimes.push(event.startDate);

            // If this event is earlier than the current primary one, make it primary
            if (event.startDate < eventGroups[groupKey].startDate) {
              eventGroups[groupKey].startDate = event.startDate;
            }
          }
        });

        // Convert the grouped object back to an array
        const uniqueEvents = Object.values(eventGroups);

        // Save the processed events to state
        setEvents(uniqueEvents);
        setDisplayedEvents(uniqueEvents.slice(0, EVENTS_PER_PAGE));

        // Cache the events data in localStorage for future use
        try {
          localStorage.setItem(cacheKey, JSON.stringify({
            events: uniqueEvents,
            mapCenter: coordinates,
            timestamp: new Date().getTime()
          }));
        } catch (cacheError) {
          console.error('Error caching events:', cacheError);
          // Continue even if caching fails
        }
      } catch (err) {
        setError('Failed to fetch events. Please try again.');
        console.error('Error fetching events:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchEventsAndUpdateMap();
  }, [searchCity, searchDate, step]);

  // Update displayed events when page changes
  useEffect(() => {
    if (currentPage >= totalPages) {
      setDisplayedEvents(events);
    } else {
      const startIndex = (currentPage - 1) * EVENTS_PER_PAGE;
      const endIndex = startIndex + EVENTS_PER_PAGE;
      setDisplayedEvents(events.slice(startIndex, endIndex));
    }
  }, [events, currentPage, totalPages]);

  // Fetch itinerary when logged in and have city/date
  useEffect(() => {
    if (!token || !user || !searchCity || !searchDate || step !== 3) {
      setItinerary([]);
      return;
    }
    axios.get(`${API_BASE_URL}/itinerary?city=${encodeURIComponent(searchCity)}&date=${searchDate}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => setItinerary(res.data))
      .catch(() => setItinerary([]));
  }, [token, user, searchCity, searchDate, step]);

  // URL parameter handling
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [initialParamsProcessed, setInitialParamsProcessed] = useState(false);

  // Only process URL parameters once on initial load
  useEffect(() => {
    if (!initialParamsProcessed) {
      const stepParam = searchParams.get('step');
      const cityParam = searchParams.get('city');
      const dateParam = searchParams.get('date');

      let shouldUpdateParams = false;
      const updates = {};

      if (stepParam) {
        setStep(Number(stepParam));
        updates.step = stepParam;
        shouldUpdateParams = true;
      }

      if (cityParam) {
        setCity(cityParam);
        setSearchCity(cityParam);
        updates.city = cityParam;
        shouldUpdateParams = true;
      }

      if (dateParam) {
        setDate(dateParam);
        setSearchDate(dateParam);
        updates.date = dateParam;
        shouldUpdateParams = true;
      }

      setInitialParamsProcessed(true);
    }
  }, [searchParams, initialParamsProcessed]);

  // Only update URL params when state changes and not from URL parameter changes
  const updateUrlParams = () => {
    if (initialParamsProcessed) {
      if (step < 3) {
        setSearchParams({ step, city, date });
      } else {
        setSearchParams({ step });
      }
    }
  };

  // Update URL when state changes
  useEffect(() => {
    if (initialParamsProcessed) {
      updateUrlParams();
    }
  }, [step, city, date, initialParamsProcessed]);

  // Event handlers
  const handleCityChange = (e) => setCity(e.target.value);
  const handleDateChange = (e) => setDate(e.target.value);
  const handleDateChangeInResults = (e) => setDate(e.target.value);

  const handleNextStep = (nextStep) => {
    setStep(nextStep);
    setError(null);
    setSuccess(null);

    // When moving to step 3, set the search values
    if (nextStep === 3) {
      setSearchCity(city);
      setSearchDate(date);
    }
  };

  const handleCityChangeInResults = (e) => {
    const value = e.target.value;
    setCity(value);

    // Filter cities based on input
    const filtered = POPULAR_CITIES.filter(cityName =>
      cityName.toLowerCase().includes(value.toLowerCase())
    );
    setFilteredCities(filtered);
    setShowCityDropdown(value.length > 0 && filtered.length > 0);
  };

  const handleCitySelectInResults = (selectedCity) => {
    setCity(selectedCity);
    setShowCityDropdown(false);
  };

  const handleCityFocusInResults = () => {
    if (city.length > 0) {
      const filtered = POPULAR_CITIES.filter(cityName =>
        cityName.toLowerCase().includes(city.toLowerCase())
      );
      setFilteredCities(filtered);
      setShowCityDropdown(filtered.length > 0);
    } else {
      setFilteredCities(POPULAR_CITIES.slice(0, 10));
      setShowCityDropdown(true);
    }
  };

  const handleSearch = () => {
    if (city && date) {
      setSearchCity(city);
      setSearchDate(date);
    }
  };

  const handleEventAction = (eventId, status) => {
    if (!token || !user) {
      setShowLoginModal(true);
      return;
    }

    const processedEventId = eventId?.toString() || eventId;

    axios.post(`${API_BASE_URL}/itinerary`, {
      eventId: processedEventId,
      city,
      date,
      status,
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(() => {
      setSuccess(`Marked as '${status.charAt(0).toUpperCase() + status.slice(1)}'!`);
      setItinerary(prev => {
        const filtered = prev.filter(i => i.eventId !== processedEventId);
        return [...filtered, { eventId: processedEventId, status }];
      });
      setTimeout(() => setSuccess(null), 3000);
    })
    .catch((err) => {
      console.error('Error updating itinerary:', err);
      const errorMessage = err.response?.data || 'Failed to update your itinerary. Please try again.';
      setError(errorMessage);
    });
  };

  const getEventStatus = (eventId) => {
    const found = itinerary.find(i => i.eventId === eventId);
    return found ? found.status : null;
  };

  const resetSearch = () => {
    setStep(1);
    setCity('');
    setDate('');
    setSearchCity('');
    setSearchDate('');
    setEvents([]);
    setItinerary([]);
    setError(null);
    setSuccess(null);
  };

  const hasValidCoordinates = (event) => {
    const lat = event.latitude || event.lat;
    const lng = event.longitude || event.lng || event.lon;
    return lat && lng && !isNaN(lat) && !isNaN(lng);
  };

  // Format date for display
  const formatDate = (startDate) => {
    try {
      return new Date(startDate).toLocaleString(undefined, {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return startDate;
    }
  };

  // Toggle expanded event details and highlight on map
  const toggleEventDetails = (eventId) => {
    const isCurrentlyExpanded = expandedEvent === eventId;
    setExpandedEvent(isCurrentlyExpanded ? null : eventId);

    // Highlight the event on map when expanded
    if (!isCurrentlyExpanded) {
      setHighlightedEvent(eventId);
      // Find the event and center map on it if it has coordinates
      const event = events.find(e => e.id === eventId);
      if (event && hasValidCoordinates(event)) {
        const lat = event.latitude || event.lat;
        const lng = event.longitude || event.lng || event.lon;
        setMapCenter([lat, lng]);
      }
    } else {
      setHighlightedEvent(null);
    }
  };

  // Navigate through event images
  const navigateImage = (eventId, direction) => {
    const event = events.find(e => e.id === eventId);
    if (!event || !event.images || event.images.length <= 1) return;

    const currentIndex = currentImageIndex[eventId] || 0;
    let newIndex;

    if (direction === 'next') {
      newIndex = (currentIndex + 1) % event.images.length;
    } else {
      newIndex = currentIndex === 0 ? event.images.length - 1 : currentIndex - 1;
    }

    setCurrentImageIndex(prev => ({
      ...prev,
      [eventId]: newIndex
    }));
  };

  // Extract available artists from events and update state
  useEffect(() => {
    if (events && events.length > 0) {
      // Collect all artists from events
      const artistSet = new Set();

      events.forEach(event => {
        if (event.artists && event.artists.length > 0) {
          event.artists.forEach(artist => artistSet.add(artist));
        }
      });

      // Convert to array and sort alphabetically
      const sortedArtists = Array.from(artistSet).sort();
      setAvailableArtists(sortedArtists);
    } else {
      setAvailableArtists([]);
    }
  }, [events]);

  // Filter displayed events by artist
  useEffect(() => {
    if (!artistFilter || artistFilter === '') {
      // No filter applied, show all events (respect pagination)
      if (currentPage >= totalPages) {
        setDisplayedEvents(events);
      } else {
        const startIndex = (currentPage - 1) * EVENTS_PER_PAGE;
        const endIndex = startIndex + EVENTS_PER_PAGE;
        setDisplayedEvents(events.slice(startIndex, endIndex));
      }
    } else {
      // Filter events by artist
      const filteredEvents = events.filter(event =>
        event.artists &&
        event.artists.some(artist =>
          artist.toLowerCase().includes(artistFilter.toLowerCase())
        )
      );

      // Update displayed events with filtered results
      setDisplayedEvents(filteredEvents);

      // Reset pagination if needed
      if (filteredEvents.length < events.length) {
        setCurrentPage(1);
      }
    }
  }, [artistFilter, events, currentPage, totalPages]);

  return (
    <div className="home-container">
      {/* Step 1: City Selection Only */}
      {step === 1 && (
        <div className="home-initial">
          <h2 className="home-initial-title">
            <SearchIcon className="inline-block w-8 h-8 mr-3 text-blue-600" />
            Discover Events
          </h2>
          <div className="home-initial-card">
            <div className="input-group mb-6">
              <label htmlFor="city" className="flex items-center text-lg font-medium text-gray-700 mb-2">
                <LocationIcon className="w-5 h-5 mr-2 text-blue-600" />
                Where are you going?
              </label>
              <div className="relative">
                <input
                  type="text"
                  id="city"
                  value={city}
                  onChange={handleCityChange}
                  onFocus={handleCityFocusInResults}
                  placeholder="Enter a city name"
                  autoComplete="off"
                  className="w-full px-4 py-3 text-lg border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-colors"
                />
                {city && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2 text-blue-600">
                    <CheckIcon className="w-6 h-6" />
                  </div>
                )}

                {showCityDropdown && (
                  <div className="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg max-h-60 overflow-y-auto">
                    {filteredCities.map((cityName, index) => (
                      <div
                        key={index}
                        className="p-3 hover:bg-blue-50 cursor-pointer flex items-center border-b border-gray-100 last:border-0"
                        onClick={() => handleCitySelectInResults(cityName)}
                      >
                        <LocationIcon className="w-4 h-4 mr-2 text-gray-500" />
                        {cityName}
                      </div>
                    ))}
                    {filteredCities.length === 0 && (
                      <div className="p-3 text-center text-gray-500">
                        No cities found
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
            <div className="button-group mt-6">
              <button
                onClick={() => handleNextStep(2)}
                disabled={!city}
                className={`w-full flex items-center justify-center px-4 py-3 text-white rounded-md text-lg font-medium transition-all ${
                  city
                    ? 'bg-blue-600 hover:bg-blue-700 shadow-md'
                    : 'bg-gray-300 cursor-not-allowed'
                }`}
              >
                Next
                <ArrowRightIcon className="ml-2 w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Step 2: Date Selection with modern styling and icons */}
      {step === 2 && (
        <div className="home-initial">
          <h2 className="home-initial-title">
            <CalendarIcon className="inline-block w-8 h-8 mr-3 text-blue-600" />
            When are you going?
          </h2>
          <div className="home-initial-card">
            <div className="input-group mb-6">
              <label htmlFor="date" className="flex items-center text-lg font-medium text-gray-700 mb-2">
                <CalendarIcon className="w-5 h-5 mr-2 text-blue-600" />
                Select a date
              </label>
              <div className="relative">
                <input
                  type="date"
                  id="date"
                  value={date}
                  onChange={handleDateChange}
                  className="w-full px-4 py-3 text-lg border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-colors"
                  min={new Date().toISOString().split('T')[0]}
                />
                {date && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2 text-blue-600">
                    <CheckIcon className="w-6 h-6" />
                  </div>
                )}
              </div>
            </div>
            <div className="button-group mt-6 flex flex-row justify-between">
              <button
                onClick={() => handleNextStep(1)}
                className="w-1/2 mr-2 flex items-center justify-center px-4 py-3 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-all text-lg font-medium"
              >
                <ArrowLeftIcon className="mr-2 w-5 h-5" />
                Back
              </button>
              <button
                onClick={() => handleNextStep(3)}
                disabled={!date}
                className={`w-1/2 ml-2 flex items-center justify-center px-4 py-3 text-white rounded-md text-lg font-medium transition-all ${
                  date
                    ? 'bg-blue-600 hover:bg-blue-700 shadow-md'
                    : 'bg-gray-300 cursor-not-allowed'
                }`}
              >
                Explore
                <SearchIcon className="ml-2 w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Step 3: Results Map and List */}
      {step === 3 && (
        <div className="home-results">
          <div className="home-search-header">
            <div className="home-search-container">
              <SearchHeader
                city={city}
                searchCity={searchCity}
                handleCityChangeInResults={handleCityChangeInResults}
                handleCityFocusInResults={handleCityFocusInResults}
                showCityDropdown={showCityDropdown}
                setShowCityDropdown={setShowCityDropdown}
                filteredCities={filteredCities}
                handleCitySelectInResults={handleCitySelectInResults}
                date={date}
                handleDateChangeInResults={handleDateChangeInResults}
                handleSearch={handleSearch}
                artistFilter={artistFilter}
                setArtistFilter={setArtistFilter}
                availableArtists={availableArtists}
              />
            </div>
          </div>
          <div className="home-results-grid">
            <div className="home-grid">
              <div className="lg:col-span-1">
                <div className="home-column-header mb-4">
                  <h3 className="home-column-title flex items-center bg-white p-3 rounded-lg shadow-sm">
                    <CalendarIcon className="w-5 h-5 mr-2 text-blue-600" />
                    <span className="font-medium">
                      <span className="text-blue-600">{searchCity}</span> • {new Date(searchDate).toLocaleDateString('en-US', {month: 'long', day: 'numeric', year: 'numeric'})}
                    </span>
                  </h3>
                </div>
                <EventList
                  events={displayedEvents}
                  onEventAction={handleEventAction}
                  getEventStatus={getEventStatus}
                  onToggleDetails={toggleEventDetails}
                  expandedEvent={expandedEvent}
                  onImageNavigate={navigateImage}
                  currentImageIndex={currentImageIndex}
                  highlightedEvent={highlightedEvent}
                  formatDate={formatDate}
                />
                {loading && <LoadingState />}
                {error && <ErrorState message={error} />}
                {displayedEvents.length === 0 && !loading && <EmptyState message="No events found. Try different criteria." />}
              </div>
              <div className="lg:col-span-2">
                <div className="sticky top-28">
                  <div className="map-container h-[600px] lg:h-[700px] rounded-lg overflow-hidden">
                    <EventMap
                      events={displayedEvents}
                      center={mapCenter}
                      toggleEventDetails={toggleEventDetails}
                      highlightedEvent={highlightedEvent}
                      formatDate={formatDate}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Login Modal */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onLoginSuccess={(newToken, newUser) => {
            setShowLoginModal(false);
            // Update context or state with new token and user
          }}
        />
      )}
      {success && <SuccessToast message={success} />}
    </div>
  );
};

export default Home;
