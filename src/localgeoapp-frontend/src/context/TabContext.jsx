import { createContext, useState, useContext } from 'react';

// Create context with default value
const TabContext = createContext({
  activeTab: 'artist', // Default to artist search
  setActiveTab: () => {},
});

// Provider component
export function TabProvider({ children }) {
  const [activeTab, setActiveTab] = useState('artist');

  return (
    <TabContext.Provider value={{ activeTab, setActiveTab }}>
      {children}
    </TabContext.Provider>t
  );
}

// Custom hook to use the context
export function useTab() {
  return useContext(TabContext);
}
