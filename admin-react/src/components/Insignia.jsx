import Icono from './Icono'

const ICONOS = { alta: 'alerta', media: 'alerta', ok: 'ok', neutro: 'reloj', terracota: 'editar' }

export default function Insignia({ tono, texto }) {
  return (
    <span className={`insignia insignia-${tono}`}>
      <Icono nombre={ICONOS[tono] ?? 'reloj'} tamano={14} />
      {texto}
    </span>
  )
}
