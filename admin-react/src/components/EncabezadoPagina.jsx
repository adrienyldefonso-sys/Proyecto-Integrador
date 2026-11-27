import { formatearHora } from '../formato'
import Icono from './Icono'

export default function EncabezadoPagina({ titulo, subtitulo, actualizado, onActualizar }) {
  return (
    <header className="pagina-cab">
      <div>
        <h1 className="pagina-titulo">{titulo}</h1>
        {subtitulo && <p className="pagina-sub">{subtitulo}</p>}
      </div>
      {onActualizar && (
        <div className="pagina-acciones">
          {actualizado && <span className="muted">Actualizado a las {formatearHora(actualizado)}</span>}
          <button type="button" className="boton secundario" onClick={onActualizar}>
            <Icono nombre="actualizar" tamano={16} />
            Actualizar
          </button>
        </div>
      )}
    </header>
  )
}
