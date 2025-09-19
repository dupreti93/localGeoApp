import { useState, useEffect, useContext, useRef } from 'react';
import { AuthContext } from '../context/AuthContext';
import axios from 'axios';
import { API_BASE_URL } from '../common/Constants';
import '../styles/MyTravel.css';

// Import component modules
import TravelMap from './MyTravel/TravelMap';
import AiAssistant from './MyTravel/AiAssistant';
import VoiceNotesTab from './MyTravel/VoiceNotesTab';
import SavedEvents from './MyTravel/SavedEvents';
import CitySelector from './MyTravel/CitySelector';
import TabNavigation from './MyTravel/TabNavigation';

const MyTravel = () => {
  // Core state
  const { token, user } = useContext(AuthContext);
  const [savedEvents, setSavedEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCity, setSelectedCity] = useState('New York'); // Default city
  const [mapCenter, setMapCenter] = useState([40.74, -73.98]); // NYC default
  const [activeSecondaryTab, setActiveSecondaryTab] = useState('voice');

  // AI Assistant state
  const [aiInput, setAiInput] = useState('');
  const promptSuggestions = [
    "Find restaurants near my events",
    "Plan a day itinerary",
    "Suggest transportation options",
    "Weather forecast for my trip",
    "Budget planning tips"
  ];

  // Voice Notes state and refs
  const [isRecording, setIsRecording] = useState(false);
  const [audioNotes, setAudioNotes] = useState([]);
  const [recordingName, setRecordingName] = useState('');
  const [currentAudio, setCurrentAudio] = useState(null);
  const [voiceNotesLoading, setVoiceNotesLoading] = useState(false);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const audioPlayerRef = useRef(null);

  // Function to get coordinates for a city using geocoding
  const getCityCoordinates = async (cityName) => {
    try {
      const response = await axios.get(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(cityName)}&limit=1`
      );

      if (response.data && response.data.length > 0) {
        const { lat, lon } = response.data[0];
        return [parseFloat(lat), parseFloat(lon)];
      }

      return [40.74, -73.98]; // Default to NYC if not found
    } catch (error) {
      console.error('Error geocoding city:', error);
      return [40.74, -73.98];
    }
  };

  // Fetch saved/interested events
  useEffect(() => {
    const fetchSavedEvents = async () => {
      if (!token) return;

      setLoading(true);
      try {
        // Use the same base endpoint as used in Home.jsx for itinerary
        const response = await axios.get(
          `${API_BASE_URL}/itinerary`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        // Filter events marked as "saved" or "interested"
        const savedOrInterestedEvents = response.data.filter(
          item => item.status === 'saved' || item.status === 'interested'
        );

        // If the response contains event details, use them directly
        if (savedOrInterestedEvents.length > 0 && savedOrInterestedEvents[0].event) {
          setSavedEvents(savedOrInterestedEvents.map(item => item.event));
        }
        // If the response only contains eventIds, fetch the details for each event
        else if (savedOrInterestedEvents.length > 0) {
          // Fetch details for each event ID
          const eventDetails = await Promise.all(
            savedOrInterestedEvents.map(async (item) => {
              try {
                const eventResponse = await axios.get(
                  `${API_BASE_URL}/events/${item.eventId}`,
                  { headers: { Authorization: `Bearer ${token}` } }
                );
                return eventResponse.data;
              } catch (err) {
                console.error(`Error fetching event ${item.eventId}:`, err);
                return null;
              }
            })
          );

          // Filter out any null responses
          setSavedEvents(eventDetails.filter(event => event !== null));
        } else {
          setSavedEvents([]);
        }
      } catch (error) {
        console.error('Error fetching saved events:', error);
        setSavedEvents([]);
      } finally {
        setLoading(false);
      }
    };

    fetchSavedEvents();
  }, [token]);

  // Fetch voice notes when tab is active
  useEffect(() => {
    const fetchVoiceNotes = async () => {
      if (!token) return;

      setVoiceNotesLoading(true);
      try {
        const response = await axios.get(
          `${API_BASE_URL}/voice-notes`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        setAudioNotes(response.data || []);
      } catch (error) {
        console.error('Error fetching voice notes:', error);
      } finally {
        setVoiceNotesLoading(false);
      }
    };

    if (activeSecondaryTab === 'voice') {
      fetchVoiceNotes();
    }
  }, [token, activeSecondaryTab]);

  // Update map center when selected city changes
  useEffect(() => {
    const updateMapCenter = async () => {
      const coordinates = await getCityCoordinates(selectedCity);
      setMapCenter(coordinates);
    };

    updateMapCenter();
  }, [selectedCity]);

  // AI Assistant handlers
  const handleAiSubmit = async (e) => {
    e.preventDefault();
    console.log('AI prompt submitted:', aiInput);
    setAiInput('');
  };

  const handlePromptClick = (prompt) => {
    setAiInput(prompt);
  };

  // Voice recording handlers
  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioChunksRef.current = [];

      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (e) => {
        if (e.data.size > 0) {
          audioChunksRef.current.push(e.data);
        }
      };

      mediaRecorder.onstop = () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        const timestamp = new Date().toISOString();
        const newNote = {
          id: `temp-${timestamp}`,
          name: recordingName || `Voice Note ${audioNotes.length + 1}`,
          date: timestamp,
          audioUrl: URL.createObjectURL(audioBlob),
          audioBlob: audioBlob
        };

        setAudioNotes([newNote, ...audioNotes]);
        setRecordingName('');
      };

      mediaRecorder.start();
      setIsRecording(true);

    } catch (error) {
      console.error('Error accessing microphone:', error);
      alert('Could not access microphone. Please check permissions.');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);

      mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop());
    }
  };

  const uploadAudioNote = async (note) => {
    if (!token) return;

    const formData = new FormData();
    formData.append('audio', note.audioBlob);
    formData.append('name', note.name);

    try {
      const response = await axios.post(
        `${API_BASE_URL}/voice-notes`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      setAudioNotes(prev =>
        prev.map(n => n.id === note.id ? response.data : n)
      );

    } catch (error) {
      console.error('Error uploading voice note:', error);
    }
  };

  const togglePlayback = (note) => {
    if (currentAudio === note.id) {
      if (audioPlayerRef.current.paused) {
        audioPlayerRef.current.play();
      } else {
        audioPlayerRef.current.pause();
      }
    } else {
      if (audioPlayerRef.current) {
        audioPlayerRef.current.pause();
      }
      setCurrentAudio(note.id);
      audioPlayerRef.current.src = note.audioUrl;
      audioPlayerRef.current.play();
    }
  };

  const deleteNote = async (noteId) => {
    if (!token) return;

    if (confirm('Are you sure you want to delete this voice note?')) {
      try {
        await axios.delete(
          `${API_BASE_URL}/voice-notes/${noteId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        setAudioNotes(audioNotes.filter(note => note.id !== noteId));
      } catch (error) {
        console.error('Error deleting voice note:', error);
      }
    }
  };

  return (
    <div className="my-travel-container">
      <h2 className="my-travel-heading">My Travel</h2>

      {/* City selector component */}
      <CitySelector selectedCity={selectedCity} setSelectedCity={setSelectedCity} />

      {/* Main content - two column layout */}
      <div className="my-travel-grid">
        {/* Left column - Map and Events */}
        <div className="my-travel-column">
          {/* Map view */}
          <TravelMap mapCenter={mapCenter} savedEvents={savedEvents} />

          {/* Saved Events component */}
          <SavedEvents loading={loading} savedEvents={savedEvents} />
        </div>

        {/* Right column - AI Assistant and Additional Features */}
        <div className="my-travel-column">
          {/* AI Assistant - Always visible */}
          <AiAssistant
            aiInput={aiInput}
            setAiInput={setAiInput}
            handleAiSubmit={handleAiSubmit}
            handlePromptClick={handlePromptClick}
            promptSuggestions={promptSuggestions}
          />

          {/* Secondary tabs for additional features */}
          <div className="my-travel-tab-container">
            {/* Tab navigation component */}
            <TabNavigation
              activeSecondaryTab={activeSecondaryTab}
              setActiveSecondaryTab={setActiveSecondaryTab}
            />

            {/* Tab content */}
            <div className="my-travel-tab-content">
              {activeSecondaryTab === 'voice' && (
                <VoiceNotesTab
                  isRecording={isRecording}
                  recordingName={recordingName}
                  setRecordingName={setRecordingName}
                  startRecording={startRecording}
                  stopRecording={stopRecording}
                  voiceNotesLoading={voiceNotesLoading}
                  audioNotes={audioNotes}
                  audioPlayerRef={audioPlayerRef}
                  currentAudio={currentAudio}
                  setCurrentAudio={setCurrentAudio}
                  togglePlayback={togglePlayback}
                  deleteNote={deleteNote}
                />
              )}

              {activeSecondaryTab === 'future' && (
                <div className="my-travel-empty-state">
                  <p>Future feature coming soon</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyTravel;
