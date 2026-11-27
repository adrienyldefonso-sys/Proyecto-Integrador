import { useEffect, useState } from 'react'
import { construirRuta, parsearRuta } from '../ruta'

export function useRuta() {
  const [hash, setHash] = useState(() => window.location.hash)

  useEffect(() => {
    const alCambiar = () => setHash(window.location.hash)
    window.addEventListener('hashchange', alCambiar)
    return () => window.removeEventListener('hashchange', alCambiar)
  }, [])

  const ir = (vista, parcelaId = null) => {
    window.location.hash = construirRuta(vista, parcelaId)
  }

  return { ...parsearRuta(hash), ir }
}
