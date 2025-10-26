import React, { useState, useRef, useEffect } from 'react';
import { API_BASE_URL } from '../common/Constants';
import '../styles/ArtistSearch.css';

const ArtistSearch = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [searchPerformed, setSearchPerformed] = useState(false);
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const searchInputRef = useRef(null);

    // Popular searches including artists, events, sports, etc.
    const popularSearches = [
        'Taylor Swift', 'Ed Sheeran', 'Coldplay', 'Bruno Mars',
        'FIFA World Cup', 'Champions League', 'NBA Finals', 'NFL',
        'Comic Con', 'Music Festival', 'Food Festival', 'Art Exhibition',
        'Broadway Shows', 'Comedy Shows', 'Rock Concert', 'Jazz Festival',
        'Tennis Tournament', 'Boxing Match', 'Marathon', 'Theater'
    ];

    // Handle input change and show suggestions
    const handleInputChange = (e) => {
        const value = e.target.value;
        setSearchTerm(value);

        if (value.length > 0) {
            const filtered = popularSearches.filter(item =>
                item.toLowerCase().includes(value.toLowerCase())
            );
            setSuggestions(filtered.slice(0, 8)); // Show max 8 suggestions
            setShowSuggestions(true);
        } else {
            setSuggestions([]);
            setShowSuggestions(false);
        }
    };

    // Handle suggestion click
    const handleSuggestionClick = (suggestion) => {
        setSearchTerm(suggestion);
        setShowSuggestions(false);
        handleSearch(null, suggestion);
    };

    // Close suggestions when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (searchInputRef.current && !searchInputRef.current.contains(event.target)) {
                setShowSuggestions(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSearch = async (e, selectedTerm = null) => {
        if (e) e.preventDefault();

        const searchQuery = selectedTerm || searchTerm;
        if (!searchQuery.trim()) {
            setError('Please enter an event or artist name');
            return;
        }

        setLoading(true);
        setError('');
        setEvents([]);
        setSearchPerformed(true);
        setShowSuggestions(false);

        try {
            const response = await fetch(`${API_BASE_URL}/events/search/artist?artistName=${encodeURIComponent(searchQuery.trim())}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            setEvents(data);
        } catch (err) {
            console.error('Error searching for events:', err);
            setError('Failed to search for events. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Date TBA';

        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return dateString;
        }
    };

    const formatPrice = (min, max, currency = 'USD') => {
        if (!min && !max) return 'Price TBA';
        if (!max) return `From $${min}`;
        if (!min) return `Up to $${max}`;
        if (min === max) return `$${min}`;
        return `$${min} - $${max}`;
    };

    const getTicketStatusDisplay = (status, message) => {
        const statusMap = {
            'onsale': { text: '🎫 Available', class: 'status-on-sale' },
            'offsale': { text: '❌ Sold Out', class: 'status-off-sale' },
            'presale': { text: '⏰ Pre-Sale', class: 'status-presale' },
            'soldout': { text: '❌ Sold Out', class: 'status-sold-out' },
            'cancelled': { text: '🚫 Cancelled', class: 'status-cancelled' },
            'postponed': { text: '⏳ Postponed', class: 'status-postponed' },
            'rescheduled': { text: '📅 Rescheduled', class: 'status-postponed' }
        };

        const statusInfo = statusMap[status?.toLowerCase()] || {
            text: message || status || 'Status Unknown',
            class: 'status-unknown'
        };

        return (
            <span className={`ticket-status ${statusInfo.class}`}>
                {statusInfo.text}
            </span>
        );
    };

    const handleBuildItinerary = (event) => {
        // Placeholder for itinerary building logic
        alert(`Building itinerary for event: ${event.name}`);
    };

    return (
        <div className="artist-search-container">
            <div className="search-hero">
                <div className="hero-content">
                    <h1 className="hero-title">Discover Events</h1>
                    <p className="hero-subtitle">Find concerts, sports, festivals, and entertainment events worldwide</p>

                    <form onSubmit={handleSearch} className="hero-search-form">
                        <div className="search-wrapper" ref={searchInputRef}>
                            <div className="search-input-container">
                                <div className="search-icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                                    </svg>
                                </div>
                                <input
                                    type="text"
                                    value={searchTerm}
                                    onChange={handleInputChange}
                                    onFocus={() => searchTerm.length > 0 && setShowSuggestions(true)}
                                    placeholder="Search for events, artists, sports, festivals..."
                                    className="hero-search-input"
                                    disabled={loading}
                                    autoComplete="off"
                                />
                                <button
                                    type="submit"
                                    className="hero-search-button"
                                    disabled={loading || !searchTerm.trim()}
                                >
                                    {loading ? (
                                        <div className="button-spinner"></div>
                                    ) : (
                                        'Search'
                                    )}
                                </button>
                            </div>

                            {showSuggestions && suggestions.length > 0 && (
                                <div className="suggestions-dropdown">
                                    {suggestions.map((suggestion, index) => (
                                        <div
                                            key={index}
                                            className="suggestion-item"
                                            onClick={() => handleSuggestionClick(suggestion)}
                                        >
                                            <div className="suggestion-icon">
                                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 6v.75m0 3v.75m0 3v.75m0 3V18m-9-5.25h5.25M7.5 15h3M3.375 5.25c-.621 0-1.125.504-1.125 1.125v3.026a2.999 2.999 0 010 5.198v3.026c0 .621.504 1.125 1.125 1.125h17.25c.621 0 1.125-.504 1.125-1.125v-3.026a2.999 2.999 0 010-5.198V6.375c0-.621-.504-1.125-1.125-1.125H3.375z" />
                                                </svg>
                                            </div>
                                            {suggestion}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </form>

                    <div className="popular-searches">
                        <p>Popular searches:</p>
                        <div className="search-tags">
                            {popularSearches.slice(0, 8).map((item, index) => (
                                <button
                                    key={index}
                                    className="search-tag"
                                    onClick={() => handleSuggestionClick(item)}
                                >
                                    {item}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            {error && (
                <div className="error-message">
                    <div className="error-icon">⚠️</div>
                    {error}
                </div>
            )}

            {loading && (
                <div className="loading-section">
                    <div className="loading-content">
                        <div className="loading-spinner">
                            <div className="spinner"></div>
                        </div>
                        <h3>Searching for {searchTerm} events...</h3>
                        <p>Finding upcoming events and shows</p>
                    </div>
                </div>
            )}

            {searchPerformed && !loading && events.length === 0 && !error && (
                <div className="no-events-section">
                    <div className="no-events-content">
                        <div className="no-events-icon">🎫</div>
                        <h3>No upcoming events found</h3>
                        <p>We couldn't find any upcoming events for "{searchTerm}"</p>
                        <div className="suggestions-text">
                            <p>Try searching for:</p>
                            <div className="search-tags">
                                {popularSearches.slice(0, 6).map((item, index) => (
                                    <button
                                        key={index}
                                        className="search-tag"
                                        onClick={() => handleSuggestionClick(item)}
                                    >
                                        {item}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {events.length > 0 && (
                <div className="events-section">
                    <div className="events-header">
                        <h2>Events for {searchTerm}</h2>
                        <p>{events.length} event{events.length !== 1 ? 's' : ''} found</p>
                    </div>

                    <div className="events-grid">
                        {events.map((event) => (
                            <div key={event.id} className="event-card">
                                {event.image && (
                                    <div className="event-image">
                                        <img src={event.image} alt={event.name} />
                                        <div className="event-overlay">
                                            <div className="event-date-badge">
                                                {formatDate(event.startDate)}
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div className="event-content">
                                    <h3 className="event-name">{event.name}</h3>

                                    <div className="event-details">
                                        {event.venue && (
                                            <div className="event-venue">
                                                <div className="venue-icon">📍</div>
                                                <div className="venue-text">
                                                    <strong>{event.venue}</strong>
                                                    {event.city && event.state && (
                                                        <span>{event.city}, {event.state}</span>
                                                    )}
                                                </div>
                                            </div>
                                        )}

                                        <div className="pricing-section">
                                            <div className="price-tag">
                                                <div className="price-icon">💰</div>
                                                <div className="price-text">
                                                    {formatPrice(event.minPrice, event.maxPrice, event.currency)}
                                                </div>
                                            </div>

                                            <div className="ticket-status-container">
                                                {getTicketStatusDisplay(event.ticketStatus, event.ticketStatusMessage)}
                                            </div>
                                        </div>

                                        {event.allPriceRanges && event.allPriceRanges.length > 1 && (
                                            <div className="price-tiers">
                                                <summary>View all ticket types</summary>
                                                <div className="price-breakdown">
                                                    {event.allPriceRanges.map((price, index) => (
                                                        <div key={index} className="price-tier">
                                                            <span className="tier-type">{price.type}</span>
                                                            <span className="tier-price">
                                                                {formatPrice(price.min, price.max, price.currency)}
                                                            </span>
                                                        </div>
                                                    ))}
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    <div className="event-actions">
                                        {event.url && (
                                            <a
                                                href={event.url}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="primary-btn buy-tickets-btn"
                                            >
                                                <span>🎫</span>
                                                Buy Tickets
                                            </a>
                                        )}
                                        <button
                                            className="secondary-btn build-itinerary-btn"
                                            onClick={() => handleBuildItinerary(event)}
                                        >
                                            <span>✨</span>
                                            Build AI Itinerary
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default ArtistSearch;
