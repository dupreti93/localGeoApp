// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\shared\BottomTray.jsx
import { useLocation, useSearchParams, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useTab } from '../../context/TabContext';
import '../../styles/BottomTray.css';

const BottomTray = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { activeTab, setActiveTab } = useTab();

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

  // Define the navigation tabs with SVG icons
  const tabs = [
    {
      key: 'explore',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" d="m20.893 13.393-1.135-1.135a2.252 2.252 0 0 1-.421-.585l-1.08-2.16a.414.414 0 0 0-.663-.107.827.827 0 0 1-.812.21l-1.273-.363a.89.89 0 0 0-.738 1.595l.587.39c.59.395.674 1.23.172 1.732l-.2.2c-.212.212-.33.498-.33.796v.41c0 .409-.11.809-.32 1.158l-1.315 2.191a2.11 2.11 0 0 1-1.81 1.025 1.055 1.055 0 0 1-1.055-1.055v-1.172c0-.92-.56-1.747-1.414-2.089l-.655-.261a2.25 2.25 0 0 1-1.383-2.46l.007-.042a2.25 2.25 0 0 1 .29-.787l.09-.15a2.25 2.25 0 0 1 2.37-1.048l1.178.236a1.125 1.125 0 0 0 1.302-.795l.208-.73a1.125 1.125 0 0 0-.578-1.315l-.665-.332-.091.091a2.25 2.25 0 0 1-1.591.659h-.18c-.249 0-.487.1-.662.274a.931.931 0 0 1-1.458-1.137l1.411-2.353a2.25 2.25 0 0 0 .286-.76m11.928 9.869A9 9 0 0 0 8.965 3.525m11.928 9.868A9 9 0 1 1 8.965 3.525" />
        </svg>
      ),
      label: 'Explore',
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
    },
  ];

  return (
    <div className="bottom-tray">
      {tabs.map(tab => (
        <button
          key={tab.key}
          onClick={tab.onClick}
          className={`bottom-tray-link ${
            activeTab === tab.key
              ? 'bottom-tray-link-active'
              : 'bottom-tray-link-inactive'
          }`}
          title={tab.label}
        >
          <div className="bottom-tray-icon">
            {tab.icon}
          </div>
          <span className="bottom-tray-label">{tab.label}</span>
        </button>
      ))}
    </div>
  );
};

export default BottomTray;
