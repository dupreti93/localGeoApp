import { useState, useEffect, useContext, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { AuthContext } from '../../context/AuthContext';
import { useTab } from '../../context/TabContext';
import axios from 'axios';
import { API_BASE_URL } from '../../common/Constants';
import '../../styles/TravelMap.css';

// Available map styles
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

// Map updater component to update the map view when center changes
export const MapUpdater = ({ center }) => {
  const map = useMap();
  const { activeTab } = useTab();

  useEffect(() => {
    if (center) {
      map.setView(center, 13);
    }
  }, [center, map]);

  // Handle map resize when tab becomes active without remounting
  useEffect(() => {
    if (activeTab === 'mytravel') {
      // Small delay to ensure DOM is settled
      const timer = setTimeout(() => {
        map.invalidateSize();
      }, 50);

      return () => clearTimeout(timer);
    }
  }, [activeTab, map]);

  return null;
};

// TileLayerWithStyleChange component to handle dynamic map style changes
const TileLayerWithStyleChange = ({ style }) => {
  return (
    <TileLayer
      attribution={MAP_STYLES[style].attribution}
      url={MAP_STYLES[style].url}
    />
  );
};

const TravelMap = ({ mapCenter, savedEvents }) => {
  const mapRef = useRef(null);
  const [mapStyle, setMapStyle] = useState('cartoLight'); // Default to CARTO Light

  return (
    <div className="travel-map-container">
      <div className="map-style-selector">
        <select
          value={mapStyle}
          onChange={(e) => setMapStyle(e.target.value)}
          className="map-style-dropdown"
        >
          {Object.entries(MAP_STYLES).map(([key, style]) => (
            <option key={key} value={key}>
              {style.name}
            </option>
          ))}
        </select>
      </div>

      <MapContainer
        center={mapCenter}
        zoom={12}
        style={{ height: '100%', width: '100%' }}
        whenCreated={(mapInstance) => {
          mapRef.current = mapInstance;
        }}
      >
        <TileLayerWithStyleChange style={mapStyle} />
        <MapUpdater center={mapCenter} />

        {savedEvents.map((event) => (
          <Marker
            key={event.id}
            position={[parseFloat(event.latitude), parseFloat(event.longitude)]}
          >
            <Popup>
              <div className="map-popup-content">
                <h3 className="map-popup-title">{event.name}</h3>
                <p className="map-popup-venue">{event.venue}</p>
                <p className="map-popup-date">
                  {new Date(event.startDate).toLocaleString()}
                </p>
                <a
                  href={event.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="map-popup-link"
                >
                  Get Tickets
                </a>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default TravelMap;
