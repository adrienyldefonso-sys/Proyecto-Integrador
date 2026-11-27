import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// Aplica el modo oscuro antes de dibujar, para que no parpadee la pantalla
try {
  if (localStorage.getItem('ojodchakra-tema') === 'oscuro') {
    document.documentElement.dataset.tema = 'oscuro'
  }
} catch {
  // Sin almacenamiento se usa el modo claro
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)