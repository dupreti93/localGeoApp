// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\MyTravel\CitySelector.jsx
import React from 'react';
import '../../styles/CitySelector.css';

const CitySelector = ({ selectedCity, setSelectedCity }) => {
  return (
    <div className="city-selector-container">
      <label className="city-selector-label">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="city-selector-icon">
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
          <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1 1 15 0Z" />
        </svg>
        Select City
      </label>
      <select
        value={selectedCity}
        onChange={(e) => setSelectedCity(e.target.value)}
        className="city-selector-select"
      >
        <option value="New York">New York</option>
        <option value="Los Angeles">Los Angeles</option>
        <option value="Chicago">Chicago</option>
        <option value="Miami">Miami</option>
        <option value="San Francisco">San Francisco</option>
      </select>
    </div>
  );
};

export default CitySelector;
