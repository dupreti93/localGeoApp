// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\shared\Nav.jsx
import { useContext, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import LoginModal from './LoginModal';
import { LocationIcon } from './Icons'; // Import the LocationIcon component
import '../../styles/Nav.css';

const Nav = () => {
  const { token, user, logout } = useContext(AuthContext);
  const [showLoginModal, setShowLoginModal] = useState(false);

  return (
    <nav className="nav-container">
      <div className="nav-inner">
        <div className="nav-content">
          <div className="nav-left">
            <NavLink to="/" className="nav-logo-link">
              <div className="nav-logo-icon">
                <LocationIcon className="nav-logo-icon-svg" />
              </div>
              <span className="nav-logo-text">
                LocalGeo
              </span>
            </NavLink>
          </div>

          <div className="nav-right">
            {token && user ? (
              <div className="nav-user">
                <div className="nav-user-info">
                  <div className="nav-user-avatar">
                    <span className="nav-user-initial">
                      {user.displayName?.charAt(0)?.toUpperCase() || 'U'}
                    </span>
                  </div>
                  <span className="nav-user-name">{user.displayName}</span>
                </div>
                <button
                  onClick={logout}
                  className="nav-logout-btn"
                >
                  Logout
                </button>
              </div>
            ) : (
              <button
                onClick={() => setShowLoginModal(true)}
                className="nav-login-btn"
              >
                Login
              </button>
            )}
          </div>
        </div>
      </div>
      {showLoginModal && <LoginModal onClose={() => setShowLoginModal(false)} />}
    </nav>
  );
};

export default Nav;
