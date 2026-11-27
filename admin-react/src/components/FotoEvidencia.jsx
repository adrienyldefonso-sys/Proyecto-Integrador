import { useRef, useState } from 'react'
import { rutaDeFoto } from '../formato'
import Icono from './Icono'

export default function FotoEvidencia({ url, descripcion }) {
  const ruta = rutaDeFoto(url)
  const dialogo = useRef(null)
  const [fallo, setFallo] = useState(false)

  if (!ruta || fallo) {
    return (
      <div className="foto-vacia">
        <Icono nombre="imagen" tamano={24} />
        <span>{ruta ? 'La foto no está disponible' : 'Sin foto registrada'}</span>
      </div>
    )
  }

  return (
    <>
      <button
        type="button"
        className="miniatura"
        onClick={() => dialogo.current?.showModal()}
        aria-label={`Ampliar la foto: ${descripcion}`}
      >
        <img src={ruta} alt={descripcion} loading="lazy" onError={() => setFallo(true)} />
        <span className="miniatura-lupa" aria-hidden="true">
          Ampliar
        </span>
      </button>

      <dialog
        ref={dialogo}
        className="visor"
        aria-label={descripcion}
        onClick={(evento) => {
          if (evento.target === dialogo.current) dialogo.current.close()
        }}
      >
        <img src={ruta} alt={descripcion} loading="lazy" />
        <div className="visor-pie">
          <span>{descripcion}</span>
          <button type="button" className="boton secundario pequeno" onClick={() => dialogo.current?.close()}>
            Cerrar
          </button>
        </div>
      </dialog>
    </>
  )
}
