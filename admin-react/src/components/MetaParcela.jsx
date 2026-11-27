import Icono from './Icono'

export default function MetaParcela({ parcela }) {
  return (
    <p className="meta">
      <span>
        <Icono nombre="hoja" tamano={14} />
        {parcela.cultivo}
      </span>
      {parcela.referencia && (
        <span>
          <Icono nombre="parcela" tamano={14} />
          {parcela.referencia}
        </span>
      )}
      <span>
        <Icono nombre="persona" tamano={14} />
        {parcela.agricultor}
      </span>
    </p>
  )
}
