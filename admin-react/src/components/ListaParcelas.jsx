import { useState } from 'react'
import { textoConteo } from '../formato'

export default function ListaParcelas({ parcelas, seleccionId, onSeleccionar }) {
  const [busqueda, setBusqueda] = useState('')
  const texto = busqueda.trim().toLowerCase()
  const visibles = parcelas.filter((p) =>
    `${p.nombre} ${p.cultivo} ${p.referencia} ${p.agricultor}`.toLowerCase().includes(texto),
  )

  return (
    <section className="panel">
      <h2>Parcelas</h2>
      <input
        className="campo"
        type="search"
        placeholder="Buscar por nombre, cultivo o agricultor"
        value={busqueda}
        onChange={(evento) => setBusqueda(evento.target.value)}
      />

      {parcelas.length === 0 && <p className="muted">Todavía no hay parcelas registradas.</p>}
      {parcelas.length > 0 && visibles.length === 0 && (
        <p className="muted">Ninguna parcela coincide con la búsqueda.</p>
      )}

      <ul className="lista">
        {visibles.map((p) => (
          <li key={p.id}>
            <button
              type="button"
              className={`tarjeta${p.id === seleccionId ? ' activa' : ''}`}
              onClick={() => onSeleccionar(p.id)}
            >
              <strong>{p.nombre}</strong>
              <span>
                {p.cultivo}
                {p.referencia ? ` · ${p.referencia}` : ''}
              </span>
              <span className="muted">
                {p.agricultor} · {textoConteo(p.total_diagnosticos)}
              </span>
            </button>
          </li>
        ))}
      </ul>
    </section>
  )
}