import { useMemo, useState } from 'react'
import { URGENCIAS, textoConteo } from '../formato'
import EstadoVacio from './EstadoVacio'
import Esqueleto from './Esqueleto'
import Icono from './Icono'
import Insignia from './Insignia'

export default function ListaParcelas({ parcelas, cargando, seleccionId, niveles, onSeleccionar }) {
  const [busqueda, setBusqueda] = useState('')
  const texto = busqueda.trim().toLowerCase()

  const visibles = useMemo(
    () =>
      (parcelas ?? []).filter((p) =>
        `${p.nombre} ${p.cultivo} ${p.referencia} ${p.agricultor}`.toLowerCase().includes(texto),
      ),
    [parcelas, texto],
  )

  const total = parcelas ? parcelas.length : 0

  return (
    <aside className="lista-col tarjeta-panel" aria-label="Lista de parcelas">
      <div className="buscador">
        <Icono nombre="buscar" tamano={18} />
        <input
          type="search"
          aria-label="Buscar parcela"
          placeholder="Buscar por nombre, cultivo o agricultor"
          value={busqueda}
          onChange={(evento) => setBusqueda(evento.target.value)}
        />
      </div>

      {cargando ? (
        <div className="parcelas-cargando" role="status" aria-label="Cargando parcelas">
          {[0, 1, 2, 3].map((i) => (
            <Esqueleto key={i} alto="92px" radio="14px" />
          ))}
        </div>
      ) : (
        <>
          <p className="contador muted">
            {texto ? `${visibles.length} de ${total} parcelas` : total === 1 ? '1 parcela' : `${total} parcelas`}
          </p>

          {total === 0 && (
            <EstadoVacio icono="parcela" titulo="Todavía no hay parcelas">
              Aparecerán cuando los agricultores las registren desde la app.
            </EstadoVacio>
          )}
          {total > 0 && visibles.length === 0 && (
            <EstadoVacio icono="buscar" titulo="Sin resultados">
              Ninguna parcela coincide con la búsqueda.
            </EstadoVacio>
          )}

          <ul className="parcelas">
            {visibles.map((p) => {
              const activa = p.id === seleccionId
              const urgencia = URGENCIAS[niveles[p.id]]
              return (
                <li key={p.id}>
                  <button
                    type="button"
                    className={`parcela${activa ? ' activa' : ''}`}
                    aria-current={activa ? 'true' : undefined}
                    onClick={() => onSeleccionar(p.id)}
                  >
                    <span className="parcela-nombre">{p.nombre}</span>
                    <span className="parcela-meta">
                      {p.cultivo}
                      {p.referencia ? ` · ${p.referencia}` : ''}
                    </span>
                    <span className="parcela-meta">{p.agricultor}</span>
                    <span className="parcela-chips">
                      {urgencia && <Insignia tono={urgencia.tono} texto={urgencia.texto} />}
                      <span className="chip">{textoConteo(p.total_diagnosticos)}</span>
                    </span>
                  </button>
                </li>
              )
            })}
          </ul>
        </>
      )}
    </aside>
  )
}
