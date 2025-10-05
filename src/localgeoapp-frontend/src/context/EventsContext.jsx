import { createContext, useState, useContext } from 'react';

// Create context for sharing selected events across components
const EventsContext = createContext({
  selectedEvents: [],
  setSelectedEvents: () => {},
  addSelectedEvent: () => {},
  removeSelectedEvent: () => {},
  isEventSelected: () => false,
  clearSelectedEvents: () => {},
});

// Provider component
export function EventsProvider({ children }) {
  const [selectedEvents, setSelectedEvents] = useState([]);

  // Add an event to selected events
  const addSelectedEvent = (event) => {
    setSelectedEvents(prev => {
      // Check if event is already selected (by id)
      const isAlreadySelected = prev.some(e => e.id === event.id);
      if (isAlreadySelected) {
        return prev; // Don't add duplicates
      }

      console.log('✅ Adding event to selected events:', event.name);
      return [...prev, event];
    });
  };

  // Remove an event from selected events
  const removeSelectedEvent = (eventId) => {
    setSelectedEvents(prev => {
      const filtered = prev.filter(e => e.id !== eventId);
      console.log('❌ Removing event from selected events:', eventId);
      return filtered;
    });
  };

  // Check if an event is selected
  const isEventSelected = (eventId) => {
    return selectedEvents.some(e => e.id === eventId);
  };

  // Clear all selected events
  const clearSelectedEvents = () => {
    console.log('🗑️ Clearing all selected events');
    setSelectedEvents([]);
  };

  return (
    <EventsContext.Provider value={{
      selectedEvents,
      setSelectedEvents,
      addSelectedEvent,
      removeSelectedEvent,
      isEventSelected,
      clearSelectedEvents
    }}>
      {children}
    </EventsContext.Provider>
  );
}

// Custom hook to use the events context
export function useEvents() {
  const context = useContext(EventsContext);
  if (!context) {
    throw new Error('useEvents must be used within an EventsProvider');
  }
  return context;
}
