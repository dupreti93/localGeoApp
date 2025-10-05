import { useState, useEffect, useContext, useRef } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useEvents } from '../context/EventsContext';
import axios from 'axios';
import { API_BASE_URL } from '../common/Constants';
import '../styles/MyTravel.css';

// Import component modules
import VoiceNotesTab from './MyTravel/VoiceNotesTab';
import CitySelector from './MyTravel/CitySelector';
import TabNavigation from './MyTravel/TabNavigation';
import ItineraryView from './MyTravel/ItineraryView';

const MyTravel = () => {
  // Core state
  const { token, user } = useContext(AuthContext);
  const { selectedEvents } = useEvents(); // Get selected events from shared context
  const [loading, setLoading] = useState(false);
  const [selectedCity, setSelectedCity] = useState('New York'); // Default city
  const [activeSecondaryTab, setActiveSecondaryTab] = useState('itinerary'); // Set itinerary as default tab

  // Voice Notes state and refs
  const [isRecording, setIsRecording] = useState(false);
  const [audioNotes, setAudioNotes] = useState([]);
  const [recordingName, setRecordingName] = useState('');
  const [currentAudio, setCurrentAudio] = useState(null);
  const [voiceNotesLoading, setVoiceNotesLoading] = useState(false);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const audioPlayerRef = useRef(null);

  // Log selected events for debugging
  useEffect(() => {
    console.log('📋 MyTravel: Selected events from context:', selectedEvents);
  }, [selectedEvents]);

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

      {/* Main content - simplified layout */}
      <div className="my-travel-content">
        {/* Tab navigation and content */}
        <div className="my-travel-tab-container">
          <TabNavigation
            activeSecondaryTab={activeSecondaryTab}
            setActiveSecondaryTab={setActiveSecondaryTab}
          />

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

            {activeSecondaryTab === 'itinerary' && (
              <ItineraryView
                token={token}
                user={user}
                loading={loading}
                setLoading={setLoading}
                activeSecondaryTab={activeSecondaryTab}
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
  );
};

export default MyTravel;
