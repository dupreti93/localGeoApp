import React, { useState, useEffect, useRef } from 'react';
import { LocationIcon, CalendarIcon, SearchIcon, FilterIcon } from '../shared/Icons';

const SearchHeader = ({
  city,
  searchCity,
  handleCityChangeInResults,
  handleCityFocusInResults,
  showCityDropdown,
  setShowCityDropdown,
  filteredCities,
  handleCitySelectInResults,
  date,
  handleDateChangeInResults,
  handleSearch,
  artistFilter,
  setArtistFilter,
  availableArtists
}) => {
  const [showArtistDropdown, setShowArtistDropdown] = useState(false);
  const artistDropdownRef = useRef(null);

  // Close artist dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (artistDropdownRef.current && !artistDropdownRef.current.contains(event.target)) {
        setShowArtistDropdown(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [artistDropdownRef]);

  return (
    <div className="home-search-header">
      <div className="home-search-container">
        <div className="flex flex-col lg:flex-row items-center gap-4">
          <div className="flex-1">
            {/* Removed the duplicate city name heading */}
          </div>

          {/* Persistent Search Controls */}
          <div className="home-search-controls">
            <div className="home-search-input-container">
              <input
                type="text"
                value={city}
                onChange={handleCityChangeInResults}
                onFocus={handleCityFocusInResults}
                onBlur={() => setTimeout(() => setShowCityDropdown(false), 200)}
                className="home-search-input"
                placeholder="Enter city"
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
              <div className="home-search-icon">
                <LocationIcon className="w-5 h-5 text-gray-500" />
              </div>

              {/* City Dropdown for Step 3 */}
              {showCityDropdown && (
                <div className="filter-dropdown">
                  {filteredCities.map((cityName, index) => (
                    <button
                      key={index}
                      type="button"
                      onClick={() => handleCitySelectInResults(cityName)}
                      className="filter-dropdown-item"
                    >
                      <LocationIcon className="w-4 h-4 mr-2 text-gray-500" />
                      {cityName}
                    </button>
                  ))}
                  {filteredCities.length === 0 && (
                    <div className="px-3 py-2 text-gray-500 text-center text-sm">
                      No cities found
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="home-search-input-container">
              <input
                type="date"
                value={date}
                onChange={handleDateChangeInResults}
                className="home-search-input"
                min={new Date().toISOString().split('T')[0]}
              />
              <div className="home-search-icon">
                <CalendarIcon className="w-5 h-5 text-gray-500" />
              </div>
            </div>

            {/* Artist Filter Dropdown */}
            <div className="home-search-input-container" ref={artistDropdownRef}>
              <input
                type="text"
                value={artistFilter}
                onChange={(e) => setArtistFilter(e.target.value)}
                onClick={() => setShowArtistDropdown(true)}
                className="home-search-input"
                placeholder="Filter by artist"
              />
              <div className="home-search-icon">
                <FilterIcon className="w-5 h-5 text-gray-500" />
              </div>

              {showArtistDropdown && availableArtists.length > 0 && (
                <div className="filter-dropdown">
                  <button
                    className="filter-dropdown-item font-medium text-gray-700"
                    onClick={() => {
                      setArtistFilter('');
                      setShowArtistDropdown(false);
                    }}
                  >
                    Clear Filter
                  </button>
                  {availableArtists.map((artist, index) => (
                    <button
                      key={index}
                      type="button"
                      onClick={() => {
                        setArtistFilter(artist);
                        setShowArtistDropdown(false);
                      }}
                      className="filter-dropdown-item"
                    >
                      {artist}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <button
              onClick={handleSearch}
              className="home-search-button"
            >
              <SearchIcon className="w-5 h-5 mr-2" />
              Search
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchHeader;
