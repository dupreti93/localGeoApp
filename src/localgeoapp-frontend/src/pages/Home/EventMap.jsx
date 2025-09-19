import React, { useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import '../../styles/MapOverrides.css'; // Import the custom map styles

// Available map styles - same as TravelMap for consistency
const MAP_STYLES = {
  cartoLight: {
    name: 'CARTO Light',
    url: 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
  },
  maptiler: {
    name: 'MapTiler Streets',
    url: 'https://api.maptiler.com/maps/streets/{z}/{x}/{y}.png?key=get_your_own_D6rA4zTHduk6KOKTXzGB',
    attribution: '&copy; <a href="https://www.maptiler.com/copyright/" target="_blank">MapTiler</a> &copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
  }
};

// Component to update map center when city changes
export const MapUpdater = ({ center }) => {
  const map = useMap();

  React.useEffect(() => {
    if (center) {
      map.setView(center, 12);
    }
  }, [center, map]);

  return null;
};

const EventMap = ({ center, events, toggleEventDetails, highlightedEvent, formatDate }) => {
  // Default to CARTO Light style for consistency with TravelMap
  const [mapStyle, setMapStyle] = useState('cartoLight');

  // Define hasValidCoordinates function locally
  const hasValidCoordinates = (event) => {
    const lat = event.latitude || event.lat;
    const lng = event.longitude || event.lng || event.lon;
    return lat && lng && !isNaN(lat) && !isNaN(lng);
  };

  return (
    <div className="home-map-container">
      <MapContainer
        center={center}
        zoom={12}
        style={{ height: '100%', width: '100%' }}
        className="modern-map"
      >
        {/* Style selector control */}
        <div className="absolute top-2 right-2 z-[1000] bg-white shadow-md rounded-md overflow-hidden">
          <select
            className="py-1 px-2 text-sm bg-white border-none focus:outline-none cursor-pointer"
            value={mapStyle}
            onChange={(e) => setMapStyle(e.target.value)}
          >
            {Object.entries(MAP_STYLES).map(([key, style]) => (
              <option key={key} value={key}>{style.name}</option>
            ))}
          </select>
        </div>

        {/* Map tile layer using the selected style */}
        <TileLayer
          attribution={MAP_STYLES[mapStyle].attribution}
          url={MAP_STYLES[mapStyle].url}
        />

        <MapUpdater center={center} />

        {events && events.filter(hasValidCoordinates).map((event) => {
          // Get coordinates regardless of property names
          const lat = event.latitude || event.lat;
          const lng = event.longitude || event.lng || event.lon;

          return (
            <Marker
              key={event.id}
              position={[parseFloat(lat), parseFloat(lng)]}
              eventHandlers={{
                click: () => toggleEventDetails(event.id)
              }}
              opacity={highlightedEvent && highlightedEvent !== event.id ? 0.5 : 1}
            >
              <Popup>
                <div className="text-center">
                  <h3 className="font-bold">{event.name}</h3>
                  <p className="text-sm">{event.venue}</p>
                  <p className="text-xs mt-1">{formatDate(event.startDate)}</p>
                  <button
                    onClick={() => toggleEventDetails(event.id)}
                    className="mt-2 text-xs text-white bg-gray-700 hover:bg-gray-600 px-2 py-1 rounded"
                  >
                    View Details
                  </button>
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
};

export default EventMap;
