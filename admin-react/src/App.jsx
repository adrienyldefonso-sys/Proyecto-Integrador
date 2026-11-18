import { useEffect, useState } from 'react'
import { esSinAcceso, obtenerParcelas } from './api'
import AvisoAcceso from './components/AvisoAcceso'
import Historial from './components/Historial'
import ListaParcelas from './components/ListaParcelas'

export default function App() {
  const [parcelas, setParcelas] = useState(null)
  const [error, setError] = useState(null)
  const [intento, setIntento] = useState(0)
  const [seleccion, setSeleccion] = useState(null)

  useEffect(() => {
    let vigente = true
    obtenerParcelas()
      .then((datos) => {
        if (vigente) setParcelas(datos)
      })
      .catch((falla) => {
        if (vigente) setError(falla)
      })
    return () => {
      vigente = false
    }
  }, [intento])

  const reintentar = () => {
    setError(null)
    setParcelas(null)
    setIntento((n) => n + 1)
  }

  let contenido
  if (error) {
    contenido = esSinAcceso(error) ? (
      <AvisoAcceso mensaje={error.message} onReintentar={reintentar} />
    ) : (
      <section className="aviso">
        <h3>No se pudo cargar el panel</h3>
        <p>{error.message}</p>
        <button type="button" className="boton" onClick={reintentar}>
          Reintentar
        </button>
      </section>
    )
  } else if (parcelas === null) {
    contenido = <p className="muted">Cargando parcelas...</p>
  } else {
    contenido = (
      <div className="disposicion">
        <ListaParcelas
          parcelas={parcelas}
          seleccionId={seleccion}
          onSeleccionar={setSeleccion}
        />
        {seleccion ? (
          <Historial key={seleccion} parcelaId={seleccion} />
        ) : (
          <section className="panel">
            <p className="muted">Elige una parcela para ver su historial de diagnósticos.</p>
          </section>
        )}
      </div>
    )
  }

  return (
    <main className="app">
      <header className="cabecera">
        <h1>OjoDChakra</h1>
        <p>Panel del agrónomo</p>
      </header>
      {contenido}
    </main>
  )
}