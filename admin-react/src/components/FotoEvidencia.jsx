import { useRef, useState } from 'react'
import { rutaDeFoto } from '../formato'

export default function FotoEvidencia({ url, descripcion }) {
  const ruta = rutaDeFoto(url)
  const dialogo = useRef(null)
  const [fallo, setFallo] = useState(false)

  if (!ruta) return <p className="muted sin-foto">Sin foto registrada.</p>
  if (fallo) return <p className="muted sin-foto">La foto no está disponible.</p>

  return (
    <>
      <button
        type="button"
        className="miniatura"
        onClick={() => dialogo.current?.showModal()}
        aria-label={`Ampliar la foto: ${descripcion}`}
      >
        <img src={ruta} alt={descripcion} loading="lazy" onError={() => setFallo(true)} />
      </button>

      <dialog
        ref={dialogo}
        className="visor"
        onClick={(evento) => {
          if (evento.target === dialogo.current) dialogo.current.close()
        }}
      >
        <img src={ruta} alt={descripcion} loading="lazy" />
        <div className="visor-pie">
          <span>{descripcion}</span>
          <button type="button" className="boton secundario" onClick={() => dialogo.current?.close()}>
            Cerrar
          </button>
        </div>
      </dialog>
    </>
  )
}