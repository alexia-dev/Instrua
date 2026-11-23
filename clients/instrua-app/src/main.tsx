import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import GeminiMobilePreview from "./GeminiMobilePreview";
import "./styles.css";
import "./gemini-preview-entry.css";

const params = new URLSearchParams(window.location.search);
const showGeminiPreview = params.get("gemini-preview") === "1";
const previewUrl = window.location.pathname + "?gemini-preview=1";

createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    {showGeminiPreview ? <GeminiMobilePreview /> : (
      <>
        <App />
        <a className="gemini-preview-launch" href={previewUrl} aria-label="Abrir prévia mobile em modo demonstração">
          Prévia mobile Gemini <span aria-hidden="true">↗</span>
        </a>
      </>
    )}
  </React.StrictMode>
);
