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
            <h1 className="home-search-title">
              Events in {searchCity}
            </h1>
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
                <LocationIcon />
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
                <CalendarIcon />
              </div>
            </div>

            {/* Artist Filter Dropdown */}
            <div className="home-search-input-container" ref={artistDropdownRef}>
              <div
                className="home-search-input cursor-pointer flex items-center justify-between"
                onClick={() => setShowArtistDropdown(!showArtistDropdown)}
              >
                <span className={artistFilter ? 'text-gray-900' : 'text-gray-400'}>
                  {artistFilter || 'Filter by Artist'}
                </span>
                <FilterIcon className="w-4 h-4 ml-1 text-gray-500" />
              </div>

              {showArtistDropdown && (
                <div className="filter-dropdown max-h-64 overflow-y-auto">
                  <div className="sticky top-0 bg-white border-b border-gray-200">
                    <button
                      type="button"
                      onClick={() => {
                        setArtistFilter('');
                        setShowArtistDropdown(false);
                      }}
                      className="filter-dropdown-item bg-gray-50 w-full text-left"
                    >
                      <span className="font-medium">All Artists</span>
                    </button>
                  </div>

                  {availableArtists.length > 0 ? (
                    availableArtists.map((artist, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => {
                          setArtistFilter(artist);
                          setShowArtistDropdown(false);
                        }}
                        className={`filter-dropdown-item ${artistFilter === artist ? 'bg-gray-100' : ''}`}
                      >
                        {artist}
                      </button>
                    ))
                  ) : (
                    <div className="px-3 py-2 text-gray-500 text-center text-sm">
                      No artists found
                    </div>
                  )}
                </div>
              )}
            </div>

            <button
              onClick={handleSearch}
              className="home-search-button"
            >
              <SearchIcon className="w-5 h-5 mr-1" />
              Search
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchHeader;
