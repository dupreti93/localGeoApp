import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import { TabProvider, useTab } from './context/TabContext.jsx';
import { EventsProvider } from './context/EventsContext.jsx';
import Home from './pages/Home.jsx';
import MyTravel from './pages/MyTravel.jsx';
import Nav from './pages/shared/Nav.jsx';
import BottomTray from './pages/shared/BottomTray.jsx';
import './App.css';

function AppContent() {
  const { activeTab } = useTab();

  return (
    <div className="app-gradient-bg">
      <div className="app-flex-container">
        <Nav />
        <main className="flex-1">
          <div style={{ display: activeTab === 'explore' ? 'block' : 'none', height: '100%' }}>
            <Home />
          </div>
          <div style={{ display: activeTab === 'mytravel' ? 'block' : 'none', height: '100%' }}>
            <MyTravel />
          </div>
        </main>
        <BottomTray />
      </div>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <EventsProvider>
          <TabProvider>
            <AppContent />
          </TabProvider>
        </EventsProvider>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;