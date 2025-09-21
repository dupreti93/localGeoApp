// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\shared\BottomTray.jsx
import { useLocation, useSearchParams, useNavigate } from 'react-router-dom';
import { useState, useEffect, useContext } from 'react';
import { useTab } from '../../context/TabContext';
import { AuthContext } from '../../context/AuthContext';
import '../../styles/BottomTray.css';

// Initialize saved events from localStorage if available
export let savedEvents = [];
try {
  const storedEvents = localStorage.getItem('savedEvents');
  if (storedEvents) {
    savedEvents = JSON.parse(storedEvents);
    console.log('Loaded savedEvents from localStorage:', savedEvents);
  }
} catch (e) {
  console.error('Error loading savedEvents from localStorage:', e);
}

// Set up a callback that components can call when events change
const eventChangeCallbacks = new Set();
export const subscribeToSavedEvents = (callback) => {
  eventChangeCallbacks.add(callback);
  return () => eventChangeCallbacks.delete(callback);
};

export const updateSavedEvents = (events) => {
  savedEvents = events;
  console.log('Updated savedEvents:', events);

  // Store in localStorage for persistence across page refreshes
  try {
    localStorage.setItem('savedEvents', JSON.stringify(events));
  } catch (e) {
    console.error('Error saving events to localStorage:', e);
  }

  // Notify subscribers
  eventChangeCallbacks.forEach(callback => callback(events));
};

const BottomTray = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { activeTab, setActiveTab } = useTab();
  const { user } = useContext(AuthContext);
  const [showBuildButton, setShowBuildButton] = useState(savedEvents.length > 0);

  // Check if the user is currently on the Home page
  const isOnHomePage = location.pathname === '/' || location.pathname === '';
  // Check if the user is currently on the My Travel page
  const isOnMyTravelPage = location.pathname === '/mytravel';

  // Track if the user has ever reached step 3
  const [reachedStep3, setReachedStep3] = useState(() => {
    return localStorage.getItem('reachedStep3') === 'true';
  });

  // Track last known valid parameters
  const [lastParams, setLastParams] = useState(() => {
    try {
      const savedParams = localStorage.getItem('lastExploreParams');
      return savedParams ? JSON.parse(savedParams) : { step: null, city: null, date: null };
    } catch (e) {
      return { step: null, city: null, date: null };
    }
  });

  // Subscribe to saved events changes
  useEffect(() => {
    console.log('Initial savedEvents check:', savedEvents);

    const updateButton = (events) => {
      console.log('Setting showBuildButton based on events:', events);
      setShowBuildButton(events && events.length > 0);
    };

    // Initial check
    updateButton(savedEvents);

    // Subscribe to changes
    const unsubscribe = subscribeToSavedEvents(updateButton);
    return unsubscribe;
  }, []);

  // Track the state for later navigation
  useEffect(() => {
    // Only track when we're on the explore tab
    if (activeTab === 'explore') {
      const step = searchParams.get('step');
      const city = searchParams.get('city');
      const date = searchParams.get('date');

      // If we reach step 3, mark that in localStorage
      if (step === '3') {
        setReachedStep3(true);
        localStorage.setItem('reachedStep3', 'true');
      }

      // Save any valid params as we navigate
      const newParams = {};

      if (step) {
        newParams.step = step;
      }

      if (city) {
        newParams.city = city;
      }

      if (date) {
        newParams.date = date;
      }

      // Only update if we have meaningful data
      if (Object.keys(newParams).length > 0) {
        const updatedParams = {...lastParams, ...newParams};
        setLastParams(updatedParams);
        localStorage.setItem('lastExploreParams', JSON.stringify(updatedParams));
      }
    }
  }, [searchParams, activeTab, lastParams]);

  // Handle Explore tab click with intelligent navigation
  const handleExploreClick = () => {
    setActiveTab('explore');

    // If we've reached step 3 before and have valid params, go directly there
    if (reachedStep3 && lastParams.city && lastParams.date) {
      navigate(`/?step=3&city=${encodeURIComponent(lastParams.city)}&date=${encodeURIComponent(lastParams.date)}`);
    }
    // If we have only city but not date, go to date selection
    else if (lastParams.city) {
      navigate(`/?step=2&city=${encodeURIComponent(lastParams.city)}`);
    }
    // Otherwise go to starting point
    else {
      navigate('/');
    }
  };

  // Handle MyTravel tab click
  const handleMyTravelClick = () => {
    setActiveTab('mytravel');
    // We keep the URL updated for bookmarking purposes, but components won't unmount
    navigate('/mytravel');
  };

  // Handle Build Itinerary click
  const handleBuildItineraryClick = async () => {
    console.log('Build itinerary clicked');

    // Show loading state
    alert('Generating your itinerary with AI... This would typically take a few moments.');

    try {
      // In a real implementation, you would:
      // 1. Make an API call to your AI service
      // 2. Pass the saved events as input to the AI
      // 3. Get back a structured itinerary

      // For now, we'll simulate the AI response with a timeout
      setTimeout(() => {
        // This is where you'd make the actual API call to your backend
        // For now, we'll just navigate to the My Travel tab
        // When you integrate with backend, you'll pass the AI-generated itinerary
        handleMyTravelClick();

        // Indicate success
        alert('Itinerary successfully generated! You can view it in My Travel.');
      }, 1500);
    } catch (error) {
      console.error('Error generating itinerary:', error);
      alert('Failed to generate itinerary. Please try again.');
    }
  };

  // Handle Add Guests click
  const handleAddGuestsClick = () => {
    // This will be implemented to add guests to the travel plan
    console.log('Add guests clicked');
    // Future functionality will be implemented here
    alert('Add guests functionality will be available soon!');
  };

  // Handle Record Expenses click
  const handleRecordExpensesClick = () => {
    // This will be implemented to record travel expenses
    console.log('Record expenses clicked');
    // Future functionality will be implemented here
    alert('Record expenses functionality will be available soon!');
  };

  // Define different tab sets based on which page we're on
  let tabs = [];

  if (isOnMyTravelPage) {
    // When on My Travel page, show Add Events, Add Guests, Record Expenses
    tabs = [
      // Add Events tab
      {
        key: 'explore',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
        ),
        label: 'Add Events',
        onClick: handleExploreClick
      },
      // Add Guests tab
      {
        key: 'addGuests',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M18 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zM3 19.235v-.11a6.375 6.375 0 0112.75 0v.109A12.318 12.318 0 019.374 21c-2.331 0-4.512-.645-6.374-1.766z" />
          </svg>
        ),
        label: 'Add Guests',
        onClick: handleAddGuestsClick
      },
      // Record Expenses tab
      {
        key: 'expenses',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 18.75a60.07 60.07 0 0115.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75M3.75 4.5v.75A.75.75 0 013 6h-.75m0 0v-.375c0-.621.504-1.125 1.125-1.125H20.25M2.25 6v9m18-10.5v.75c0 .414.336.75.75.75h.75m-1.5-1.5h.375c.621 0 1.125.504 1.125 1.125v9.75c0 .621-.504 1.125-1.125 1.125h-.375m1.5-1.5H21a.75.75 0 00-.75.75v.75m0 0H3.75m0 0h-.375a1.125 1.125 0 01-1.125-1.125V15m1.5 1.5v-.75A.75.75 0 003 15h-.75M15 10.5a3 3 0 11-6 0 3 3 0 016 0zm3 0h.008v.008H18V10.5zm-12 0h.008v.008H6V10.5z" />
          </svg>
        ),
        label: 'Expenses',
        onClick: handleRecordExpensesClick
      }
    ];
  } else if (isOnHomePage) {
    // When on Home page, show My Travel and possibly Build Itinerary
    tabs = [
      // My Travel tab
      {
        key: 'mytravel',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498 4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 0 0-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0Z" />
          </svg>
        ),
        label: 'My Travel',
        onClick: handleMyTravelClick
      }
    ];

    // Add Build Itinerary button only on Home page and only if we have saved events
    if (showBuildButton) {
      tabs.push({
        key: 'buildItinerary',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 6.878V6a2.25 2.25 0 0 1 2.25-2.25h7.5A2.25 2.25 0 0 1 18 6v.878m-12 0c.235-.083.487-.128.75-.128h10.5c.263 0 .515.045.75.128m-12 0A2.25 2.25 0 0 0 4.5 9v.878m13.5-3A2.25 2.25 0 0 1 19.5 9v.878m0 0a2.246 2.246 0 0 0-.75-.128H5.25c-.263 0-.515.045-.75.128m15 0A2.25 2.25 0 0 1 21 12v6a2.25 2.25 0 0 1-2.25 2.25H5.25A2.25 2.25 0 0 1 3 18v-6c0-.98.626-1.813 1.5-2.122" />
          </svg>
        ),
        label: 'Build Itinerary',
        onClick: handleBuildItineraryClick,
        isBig: true // Flag to render this button bigger
      });
    }
  } else {
    // On other pages, show both main navigation options
    tabs = [
      {
        key: 'explore',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
        ),
        label: 'Add Events',
        onClick: handleExploreClick
      },
      {
        key: 'mytravel',
        icon: (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498 4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 0 0-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0Z" />
          </svg>
        ),
        label: 'My Travel',
        onClick: handleMyTravelClick
      }
    ];
  }

  return (
    <div className="bottom-tray">
      {tabs.map(tab => (
        <button
          key={tab.key}
          onClick={tab.onClick}
          className={`bottom-tray-link ${
            tab.isBig ? 'bottom-tray-link-big' : ''
          } ${
            activeTab === tab.key
              ? 'bottom-tray-link-active'
              : 'bottom-tray-link-inactive'
          }`}
          title={tab.label}
        >
          <div className={`${tab.isBig ? 'bottom-tray-big-icon' : 'bottom-tray-icon'}`}>
            {tab.icon}
          </div>
          <span className={`${tab.isBig ? 'bottom-tray-big-label' : 'bottom-tray-label'}`}>
            {tab.label}
          </span>
        </button>
      ))}
    </div>
  );
};

export default BottomTray;
