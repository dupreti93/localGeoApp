import { createContext, useState, useContext } from 'react';

// Create context with default value
const TabContext = createContext({
  activeTab: 'explore', // Default tab
  setActiveTab: () => {},
});

// Provider component
export function TabProvider({ children }) {
  const [activeTab, setActiveTab] = useState('explore');

  return (
    <TabContext.Provider value={{ activeTab, setActiveTab }}>
      {children}
    </TabContext.Provider>
  );
}

// Custom hook to use the context
export function useTab() {
  return useContext(TabContext);
}
