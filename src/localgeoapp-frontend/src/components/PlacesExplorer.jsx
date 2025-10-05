import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../common/Constants';
import '../styles/PlacesExplorer.css';

const PlacesExplorer = ({ location, latitude, longitude, savedPlaces = [], onAddPlace }) => {
  const [activeCategory, setActiveCategory] = useState('restaurants');
  const [places, setPlaces] = useState({
    restaurants: [],
    attractions: []
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const categories = [
    {
      id: 'restaurants',
      name: 'Food/Restaurants',
      icon: '🍴',
      endpoint: '/places/restaurants'
    },
    {
      id: 'attractions',
      name: 'Parks/Museums/Nature',
      icon: '🏛️',
      endpoint: '/places/attractions'
    }
  ];

  const fetchPlaces = async (category) => {
    if (Array.isArray(places[category]) && places[category].length > 0) return; // Don't fetch if already loaded

    setLoading(true);
    setError(null);

    try {
      const categoryConfig = categories.find(cat => cat.id === category);
      const params = {
        location: location || 'New York',
        ...(latitude && longitude && { latitude, longitude })
      };

      const response = await axios.get(`${API_BASE_URL}${categoryConfig.endpoint}`, { params });

      setPlaces(prev => ({
        ...prev,
        [category]: Array.isArray(response.data) ? response.data : []
      }));
    } catch (err) {
      console.error(`Error fetching ${category}:`, err);
      setError(`Failed to load ${category}. Please try again.`);
      // Set empty array on error to prevent further issues
      setPlaces(prev => ({
        ...prev,
        [category]: []
      }));
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryChange = (categoryId) => {
    setActiveCategory(categoryId);
    fetchPlaces(categoryId);
  };

  // Load initial category on mount
  useEffect(() => {
    fetchPlaces(activeCategory);
  }, [location, latitude, longitude]);

  // Check if a place is already saved
  const isPlaceSaved = (placeId) => {
    return savedPlaces && savedPlaces.some(saved => saved.id === placeId || saved.placeId === placeId);
  };

  const renderPlaceCard = (place) => {
    const getPlaceDetails = () => {
      switch (place.source) {
        case 'yelp':
          return {
            rating: place.rating,
            reviewText: `${place.reviewCount} reviews`,
            address: place.displayAddress || place.address,
            price: place.price,
            phone: place.phone,
            image: place.imageUrl
          };
        case 'google_places':
          return {
            rating: place.rating,
            reviewText: `${place.userRatingsTotal} reviews`,
            address: place.address || place.vicinity,
            price: place.priceLevel ? '$'.repeat(place.priceLevel) : '',
            image: place.photoReference ?
              `https://maps.googleapis.com/maps/api/place/photo?maxwidth=300&photoreference=${place.photoReference}&key=YOUR_API_KEY` : null
          };
        default: // Ticketmaster events
          return {
            rating: null,
            reviewText: place.venue?.name || '',
            address: place.venue?.address || '',
            price: place.priceRange || '',
            date: place.dates?.start?.localDate,
            time: place.dates?.start?.localTime,
            image: place.images?.[0]?.url
          };
      }
    };

    const details = getPlaceDetails();

    return (
      <div key={place.id} className="place-card">
        {details.image && (
          <div className="place-image">
            <img src={details.image} alt={place.name} />
          </div>
        )}
        <div className="place-content">
          <h3 className="place-name">{place.name}</h3>

          {details.rating && (
            <div className="place-rating">
              <span className="stars">{'★'.repeat(Math.floor(details.rating))}</span>
              <span className="rating-text">{details.rating} ({details.reviewText})</span>
            </div>
          )}

          <p className="place-address">{details.address}</p>

          {details.date && (
            <p className="place-date">
              📅 {details.date} {details.time && `at ${details.time}`}
            </p>
          )}

          {details.price && (
            <p className="place-price">💰 {details.price}</p>
          )}

          {details.phone && (
            <p className="place-phone">📞 {details.phone}</p>
          )}

          <div className="place-actions">
            {isPlaceSaved(place.id) ? (
              <div className="px-3 py-1 bg-green-100 text-green-700 text-sm rounded text-center w-full">
                Added to My Places
              </div>
            ) : (
              <button
                onClick={() => onAddPlace && onAddPlace(place.id)}
                className="btn-primary w-full"
              >
                ➕ Add to My Places
              </button>
            )}
            <button className="btn-secondary mt-2">View Details</button>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="places-explorer">
      <div className="category-tabs">
        {categories.map(category => (
          <button
            key={category.id}
            className={`category-tab ${activeCategory === category.id ? 'active' : ''}`}
            onClick={() => handleCategoryChange(category.id)}
          >
            <span className="category-icon">{category.icon}</span>
            <span className="category-name">{category.name}</span>
          </button>
        ))}
      </div>

      <div className="places-content">
        {loading && (
          <div className="loading-state">
            <div className="loading-spinner"></div>
            <p>Loading {categories.find(cat => cat.id === activeCategory)?.name}...</p>
          </div>
        )}

        {error && (
          <div className="error-state">
            <p className="error-message">{error}</p>
            <button
              className="btn-primary"
              onClick={() => fetchPlaces(activeCategory)}
            >
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && (
          <div className="places-grid">
            {Array.isArray(places[activeCategory]) && places[activeCategory].map(place => renderPlaceCard(place))}

            {Array.isArray(places[activeCategory]) && places[activeCategory].length === 0 && (
              <div className="empty-state">
                <p>No {categories.find(cat => cat.id === activeCategory)?.name.toLowerCase()} found in this area.</p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default PlacesExplorer;
