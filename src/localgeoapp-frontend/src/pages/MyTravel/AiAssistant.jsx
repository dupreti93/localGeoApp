import React from 'react';
import '../../styles/AiAssistant.css';

const AiAssistant = ({ aiInput, setAiInput, handleAiSubmit, handlePromptClick, promptSuggestions }) => {
  return (
    <div className="ai-assistant-container">
      <h3 className="ai-assistant-header">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="ai-assistant-icon">
          <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09Z" />
          <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 5.25 18 6.75m-1.5-1.5L18 3.75m-1.5 1.5L15 6.75m1.5-1.5L15 3.75" />
        </svg>
        AI Travel Assistant
      </h3>

      {/* Prompt pills */}
      <div className="ai-prompt-suggestions">
        {promptSuggestions.map((prompt, index) => (
          <button
            key={index}
            onClick={() => handlePromptClick(prompt)}
            className="ai-prompt-pill"
          >
            {prompt}
          </button>
        ))}
      </div>

      {/* AI input form */}
      <form onSubmit={handleAiSubmit} className="ai-input-form">
        <input
          type="text"
          value={aiInput}
          onChange={(e) => setAiInput(e.target.value)}
          placeholder="How can I help with your travel plans?"
          className="ai-input-field"
        />
        <button
          type="submit"
          className="ai-submit-button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="ai-submit-icon">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5" />
          </svg>
          Send
        </button>
      </form>
    </div>
  );
};

export default AiAssistant;
