import { useState } from 'react'
import Tablero from './components/Tablero'
import VistaParcelas from './components/VistaParcelas'

export default function App() {
  const [vista, setVista] = useState('alertas')
  const [seleccion, setSeleccion] = useState(null)

  const verHistorial = (parcelaId) => {
    setSeleccion(parcelaId)
    setVista('parcelas')
  }

  return (
    <main className="app">
      <header className="cabecera">
        <h1>OjoDChakra</h1>
        <p>Panel del agrónomo</p>
      </header>

      <nav className="pestanas" aria-label="Secciones del panel">
        <button
          type="button"
          className={`pestana${vista === 'alertas' ? ' activa' : ''}`}
          onClick={() => setVista('alertas')}
        >
          Alertas y ruta
        </button>
        <button
          type="button"
          className={`pestana${vista === 'parcelas' ? ' activa' : ''}`}
          onClick={() => setVista('parcelas')}
        >
          Parcelas
        </button>
      </nav>

      {vista === 'alertas' ? (
        <Tablero onVerHistorial={verHistorial} />
      ) : (
        <VistaParcelas seleccion={seleccion} onSeleccionar={setSeleccion} />
      )}
    </main>
  )
}