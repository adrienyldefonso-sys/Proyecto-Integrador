import { esSinAcceso } from '../api'
import AvisoAcceso from './AvisoAcceso'
import Icono from './Icono'

export default function ErrorCarga({ error, titulo = 'No se pudo cargar la información', onReintentar }) {
  if (esSinAcceso(error)) {
    return <AvisoAcceso mensaje={error.message} onReintentar={onReintentar} />
  }

  return (
    <section className="error-carga" role="alert">
      <span className="vacio-icono vacio-icono-alerta">
        <Icono nombre="alerta" tamano={28} />
      </span>
      <h3>{titulo}</h3>
      <p>{error.message}</p>
      <button type="button" className="boton" onClick={onReintentar}>
        <Icono nombre="actualizar" tamano={16} />
        Reintentar
      </button>
    </section>
  )
}
