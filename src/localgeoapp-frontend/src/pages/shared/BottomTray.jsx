// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\shared\BottomTray.jsx
import { useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import { useTab } from '../../context/TabContext';
import '../../styles/BottomTray.css';

const BottomTray = () => {
  const navigate = useNavigate();
  const { activeTab, setActiveTab } = useTab();

  // Handle Discover Events tab click
  const handleDiscoverEventsClick = () => {
    setActiveTab('artist');
    navigate('/');
  };

  // Handle My Itineraries tab click
  const handleMyItinerariesClick = () => {
    setActiveTab('mytravel');
    navigate('/mytravel');
  };

  // Two main tabs: Discover Events and My Itineraries
  const tabs = [
    {
      key: 'artist',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
        </svg>
      ),
      label: 'Discover Events',
      onClick: handleDiscoverEventsClick
    },
    {
      key: 'mytravel',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498 4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 0 0-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0Z" />
        </svg>
      ),
      label: 'My Itineraries',
      onClick: handleMyItinerariesClick
    }
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
          <span className="bottom-tray-label">
            {tab.label}
          </span>
        </button>
      ))}
    </div>
  );
};

export default BottomTray;
