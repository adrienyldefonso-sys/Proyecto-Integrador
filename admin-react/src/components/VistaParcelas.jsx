import { useEffect, useState } from 'react'
import { esSinAcceso, obtenerParcelas } from '../api'
import AvisoAcceso from './AvisoAcceso'
import Historial from './Historial'
import ListaParcelas from './ListaParcelas'

export default function VistaParcelas({ seleccion, onSeleccionar }) {
  const [parcelas, setParcelas] = useState(null)
  const [error, setError] = useState(null)
  const [intento, setIntento] = useState(0)

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

  if (error) {
    return esSinAcceso(error) ? (
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
  }

  if (parcelas === null) {
    return <p className="muted">Cargando parcelas...</p>
  }

  return (
    <div className="disposicion">
      <ListaParcelas
        parcelas={parcelas}
        seleccionId={seleccion}
        onSeleccionar={onSeleccionar}
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