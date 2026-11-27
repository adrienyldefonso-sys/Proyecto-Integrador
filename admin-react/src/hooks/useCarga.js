import { useEffect, useRef, useState } from 'react'

// Carga datos de la API y los mantiene al día.
// - `dependencias`: si cambian, se vuelve a cargar y se muestra el estado de carga.
// - `refrescoMs`: recarga silenciosa cada cierto tiempo. No vuelve a mostrar "cargando" y,
//   si falla, conserva lo que ya se estaba mostrando.
export function useCarga(cargar, dependencias, { refrescoMs = 0 } = {}) {
  const [version, setVersion] = useState(0)
  const [resultado, setResultado] = useState({ clave: null })
  const [actualizado, setActualizado] = useState(null)
  const cargarRef = useRef(cargar)
  const clave = JSON.stringify(dependencias)

  useEffect(() => {
    cargarRef.current = cargar
  })

  useEffect(() => {
    let vigente = true
    cargarRef
      .current()
      .then((datos) => {
        if (!vigente) return
        setResultado({ clave, datos })
        setActualizado(new Date())
      })
      .catch((error) => {
        if (!vigente) return
        setResultado((previo) =>
          previo.clave === clave && previo.datos ? previo : { clave, error },
        )
      })
    return () => {
      vigente = false
    }
  }, [clave, version])

  useEffect(() => {
    if (!refrescoMs) return undefined
    const temporizador = setInterval(() => {
      if (!document.hidden) setVersion((n) => n + 1)
    }, refrescoMs)
    return () => clearInterval(temporizador)
  }, [refrescoMs])

  const cargando = resultado.clave !== clave
  return {
    datos: cargando ? null : (resultado.datos ?? null),
    error: cargando ? null : (resultado.error ?? null),
    cargando,
    actualizado,
    recargar: () => setVersion((n) => n + 1),
  }
}
