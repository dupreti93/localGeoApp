// filepath: c:\Users\divya\OneDrive\Documents\Projects\localGeoApp\src\localgeoapp-frontend\src\pages\shared\StateComponents.jsx
import React from 'react';
import { AlertIcon, CalendarIcon } from './Icons';

export const LoadingState = () => (
  <div className="home-loading">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-500"></div>
  </div>
);

export const ErrorState = ({ error, handleSearch }) => (
  <div className="home-error-container">
    <div className="home-error-card">
      <AlertIcon className="text-gray-500 mx-auto mb-4" />
      <h3 className="text-lg font-medium text-gray-800">Error</h3>
      <p className="mt-2 text-gray-600">{error}</p>
      <button
        onClick={handleSearch}
        className="mt-4 px-4 py-2 bg-gray-700 text-white rounded-md text-sm hover:bg-gray-600"
      >
        Try Again
      </button>
    </div>
  </div>
);

export const EmptyState = () => (
  <div className="home-error-container">
    <div className="home-error-card">
      <CalendarIcon className="w-12 h-12 text-gray-500 mx-auto mb-4" />
      <h3 className="text-lg font-medium text-gray-800">No Events Found</h3>
      <p className="mt-2 text-gray-600">Try a different city or date.</p>
    </div>
  </div>
);

export const SuccessToast = ({ message }) => (
  <div className="success-toast">
    {message}
  </div>
);
