import Icono from './Icono'

export default function EstadoVacio({ icono, titulo, children, accion }) {
  return (
    <div className="vacio">
      <span className="vacio-icono">
        <Icono nombre={icono} tamano={28} />
      </span>
      <h3>{titulo}</h3>
      {children && <p>{children}</p>}
      {accion}
    </div>
  )
}
