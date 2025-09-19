// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\Home\Filter.jsx
import React, { useState } from 'react';
import '../../styles/Filter.css';
import { LocationIcon, CalendarIcon, ArrowRightIcon } from '../shared/Icons';

const POPULAR_CITIES = [
  'New York',
  'Los Angeles',
  'Chicago',
  'Houston',
  'Phoenix',
  'Philadelphia',
  'San Antonio',
  'San Diego',
  'Dallas',
  'San Jose',
  'Austin',
  'Jacksonville',
  'Fort Worth',
  'Columbus',
  'Charlotte',
  'San Francisco',
  'Indianapolis',
  'Seattle',
  'Denver',
  'Boston',
  'Las Vegas',
  'Nashville',
  'Miami',
  'Atlanta',
  'Washington',
  'London',
  'Paris',
  'Berlin',
  'Tokyo',
  'Sydney'
];

const Filter = ({ city, date, onCityChange, onDateChange, step, onNextStep }) => {
  const [showDropdown, setShowDropdown] = useState(false);
  const [filteredCities, setFilteredCities] = useState(POPULAR_CITIES);

  const handleCityInputChange = (e) => {
    const value = e.target.value;
    onCityChange(e);

    // Filter cities based on input
    const filtered = POPULAR_CITIES.filter(cityName =>
      cityName.toLowerCase().includes(value.toLowerCase())
    );
    setFilteredCities(filtered);
    setShowDropdown(value.length > 0 && filtered.length > 0);
  };

  const selectCity = (cityName) => {
    const e = { target: { value: cityName } };
    onCityChange(e);
    setShowDropdown(false);
  };

  const handleDateChange = (e) => {
    onDateChange(e);
  };

  const handleClickOutside = () => {
    setShowDropdown(false);
  };

  // Render different steps based on current step value
  const renderStep = () => {
    switch(step) {
      case 1:
        return (
          <div className="flex flex-col">
            <label className="filter-label flex items-center mb-2">
              <LocationIcon className="w-5 h-5 mr-2 text-gray-600 flex-shrink-0" />
              <span>Where do you want to explore?</span>
            </label>
            <div className="relative">
              <input
                type="text"
                value={city}
                onChange={handleCityInputChange}
                placeholder="Enter a city"
                className="filter-input"
              />
              {showDropdown && (
                <div className="filter-dropdown">
                  {filteredCities.map((cityName, index) => (
                    <div
                      key={index}
                      onClick={() => selectCity(cityName)}
                      className="filter-dropdown-item"
                    >
                      <LocationIcon className="w-4 h-4 mr-2 text-gray-500" />
                      {cityName}
                    </div>
                  ))}
                </div>
              )}
            </div>
            {city && (
              <button
                onClick={() => onNextStep(step + 1)}
                className="filter-button"
              >
                <ArrowRightIcon className="w-4 h-4 mr-1" />
                Next
              </button>
            )}
          </div>
        );
      case 2:
        return (
          <div className="flex flex-col">
            <label className="filter-label flex items-center mb-2">
              <CalendarIcon className="w-5 h-5 mr-2 text-gray-600 flex-shrink-0" />
              <span>When do you want to explore {city}?</span>
            </label>
            <input
              type="date"
              value={date}
              onChange={handleDateChange}
              min={new Date().toISOString().split('T')[0]}
              className="filter-input"
            />
            {date && (
              <button
                onClick={() => onNextStep(step + 1)}
                className="filter-button"
              >
                <ArrowRightIcon className="w-4 h-4 mr-1" />
                Find Events
              </button>
            )}
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="filter-container">
      {renderStep()}
    </div>
  );
};

export default Filter;
