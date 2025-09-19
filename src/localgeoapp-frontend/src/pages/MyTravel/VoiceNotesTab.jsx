import React, { useRef } from 'react';
import '../../styles/VoiceNotesTab.css';

const VoiceNotesTab = ({
  isRecording,
  recordingName,
  setRecordingName,
  startRecording,
  stopRecording,
  voiceNotesLoading,
  audioNotes,
  audioPlayerRef,
  currentAudio,
  togglePlayback,
  deleteNote,
  setCurrentAudio
}) => {
  return (
    <div className="voice-notes-container">
      {/* Recording UI */}
      <div className="recording-section">
        <div className="recording-header">
          <h3 className="section-title">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="section-icon">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 18.75a6 6 0 0 0 6-6v-1.5m-6 7.5a6 6 0 0 1-6-6v-1.5m6 7.5v3.75m-3.75 0h7.5M12 15.75a3 3 0 0 1-3-3V4.5a3 3 0 1 1 6 0v8.25a3 3 0 0 1-3 3Z" />
            </svg>
            Record Voice Note
          </h3>
          <button
            onClick={isRecording ? stopRecording : startRecording}
            className={`recording-button ${
              isRecording
                ? 'recording-button-active'
                : 'recording-button-inactive'
            }`}
          >
            {isRecording ? (
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="white" className="recording-button-icon">
                <path strokeLinecap="round" strokeLinejoin="round" d="M5.25 7.5A2.25 2.25 0 0 1 7.5 5.25h9a2.25 2.25 0 0 1 2.25 2.25v9a2.25 2.25 0 0 1-2.25 2.25h-9a2.25 2.25 0 0 1-2.25-2.25v-9Z" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="white" className="recording-button-icon">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 18.75a6 6 0 0 0 6-6v-1.5m-6 7.5a6 6 0 0 1-6-6v-1.5m6 7.5v3.75m-3.75 0h7.5M12 15.75a3 3 0 0 1-3-3V4.5a3 3 0 1 1 6 0v8.25a3 3 0 0 1-3 3Z" />
              </svg>
            )}
          </button>
        </div>

        <input
          type="text"
          value={recordingName}
          onChange={(e) => setRecordingName(e.target.value)}
          placeholder="Note name (optional)"
          className="recording-input"
          disabled={isRecording}
        />

        {isRecording && (
          <p className="recording-status">
            <span className="recording-indicator"></span>
            Recording in progress...
          </p>
        )}
      </div>

      {/* Voice notes list */}
      <div>
        <h3 className="section-title">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="section-icon">
            <path strokeLinecap="round" strokeLinejoin="round" d="M19.114 5.636a9 9 0 0 1 0 12.728M16.463 8.288a5.25 5.25 0 0 1 0 7.424M6.75 8.25l4.72-4.72a.75.75 0 0 1 1.28.53v15.88a.75.75 0 0 1-1.28.53l-4.72-4.72H4.51c-.88 0-1.704-.507-1.938-1.354A9.009 9.009 0 0 1 2.25 12c0-.83.112-1.633.322-2.396C2.806 8.756 3.63 8.25 4.51 8.25H6.75Z" />
          </svg>
          My Voice Notes
        </h3>

        {/* Hidden audio player for playing notes */}
        <audio ref={audioPlayerRef} onEnded={() => setCurrentAudio(null)} />

        {voiceNotesLoading ? (
          <div className="loading-indicator">
            <div className="loading-spinner"></div>
          </div>
        ) : audioNotes.length > 0 ? (
          <div className="notes-list">
            {audioNotes.map((note) => (
              <div
                key={note.id}
                className="voice-note"
              >
                <div className="voice-note-header">
                  <div className="voice-note-info">
                    <h4 className="voice-note-name">{note.name}</h4>
                    <p className="voice-note-date">
                      {new Date(note.date).toLocaleString()}
                    </p>
                  </div>

                  <div className="voice-note-actions">
                    <button
                      onClick={() => togglePlayback(note)}
                      className={`voice-note-button ${
                        currentAudio === note.id && !audioPlayerRef.current?.paused
                          ? 'voice-note-button-active'
                          : ''
                      }`}
                    >
                      {currentAudio === note.id && !audioPlayerRef.current?.paused ? (
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="voice-note-button-icon">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 5.25v13.5m-7.5-13.5v13.5" />
                        </svg>
                      ) : (
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="voice-note-button-icon">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M5.25 5.653c0-.856.917-1.398 1.667-.986l11.54 6.347a1.125 1.125 0 0 1 0 1.972l-11.54 6.347a1.125 1.125 0 0 1-1.667-.986V5.653Z" />
                        </svg>
                      )}
                    </button>

                    <button
                      onClick={() => deleteNote(note.id)}
                      className="voice-note-button"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="voice-note-button-icon">
                        <path strokeLinecap="round" strokeLinejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <p>No voice notes yet.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default VoiceNotesTab;
