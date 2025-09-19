import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';

// Dynamically add Font Awesome kit script
const script = document.createElement('script');
script.src = 'https://kit.fontawesome.com/5511ddaf64.js'; // Replace with your kit ID
script.crossOrigin = 'anonymous';
script.async = true; // Load asynchronously to avoid blocking
document.head.appendChild(script);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);