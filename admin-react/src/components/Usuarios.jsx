import { useMemo, useState } from 'react'
import { obtenerUsuarios } from '../api'
import { textoRelativo } from '../formato'
import { useCarga } from '../hooks/useCarga'
import { FILTROS_CUENTAS, filtrarCuentas, inicialesDe } from '../usuarios'
import EncabezadoPagina from './EncabezadoPagina'
import ErrorCarga from './ErrorCarga'
import EstadoVacio from './EstadoVacio'
import Esqueleto from './Esqueleto'
import Icono from './Icono'
import Insignia from './Insignia'
import UsuarioDetalle from './UsuarioDetalle'

export default function Usuarios({ usuarioId, onSeleccionar, onVolver }) {
  const cuentas = useCarga(obtenerUsuarios, [], { refrescoMs: 60000 })
  const [texto, setTexto] = useState('')
  const [filtro, setFiltro] = useState('todos')
  const total = cuentas.datos ? cuentas.datos.length : 0
  const visibles = useMemo(
    () => filtrarCuentas(cuentas.datos ?? [], { texto, filtro }),
    [cuentas.datos, texto, filtro],
  )
  const filtrando = texto.trim() !== '' || filtro !== 'todos'

  const cabecera = (
    <EncabezadoPagina
      titulo="Usuarios"
      subtitulo="Quién usa la app, cuándo entra y qué tiene registrado."
      actualizado={cuentas.actualizado}
      onActualizar={cuentas.recargar}
    />
  )

  if (cuentas.error) {
    return (
      <>
        {cabecera}
        <ErrorCarga
          error={cuentas.error}
          titulo="No se pudieron cargar los usuarios"
          onReintentar={cuentas.recargar}
        />
      </>
    )
  }

  return (
    <>
      {cabecera}
      <div className={`maestro-detalle${usuarioId ? ' con-seleccion' : ''}`}>
        <aside className="lista-col tarjeta-panel" aria-label="Lista de usuarios">
          <div className="buscador">
            <Icono nombre="buscar" tamano={18} />
            <input
              type="search"
              aria-label="Buscar usuario"
              placeholder="Buscar por nombre, usuario o celular"
              value={texto}
              onChange={(evento) => setTexto(evento.target.value)}
            />
          </div>

          <div className="segmentos">
            {FILTROS_CUENTAS.map(([clave, etiqueta]) => (
              <button
                key={clave}
                type="button"
                className={`segmento-btn${filtro === clave ? ' activa' : ''}`}
                aria-pressed={filtro === clave}
                onClick={() => setFiltro(clave)}
              >
                {etiqueta}
              </button>
            ))}
          </div>

          {cuentas.cargando ? (
            <div className="parcelas-cargando" role="status" aria-label="Cargando usuarios">
              {[0, 1, 2, 3].map((i) => (
                <Esqueleto key={i} alto="112px" radio="14px" />
              ))}
            </div>
          ) : (
            <>
              <p className="contador muted">
                {filtrando ? `${visibles.length} de ${total} usuarios` : total === 1 ? '1 usuario' : `${total} usuarios`}
              </p>

              {total === 0 && (
                <EstadoVacio icono="persona" titulo="Todavía no hay usuarios">
                  Aparecerán cuando alguien cree una cuenta desde la app.
                </EstadoVacio>
              )}
              {total > 0 && visibles.length === 0 && (
                <EstadoVacio icono="buscar" titulo="Sin resultados">
                  Ningún usuario coincide con la búsqueda o el filtro.
                </EstadoVacio>
              )}

              <ul className="parcelas">
                {visibles.map((cuenta) => {
                  const activa = cuenta.id === usuarioId
                  return (
                    <li key={cuenta.id}>
                      <button
                        type="button"
                        className={`parcela${activa ? ' activa' : ''}`}
                        aria-current={activa ? 'true' : undefined}
                        onClick={() => onSeleccionar(cuenta.id)}
                      >
                        <span className="fila-usuario">
                          <span className="avatar" aria-hidden="true">
                            {inicialesDe(cuenta.nombre, cuenta.usuario)}
                          </span>
                          <span className="fila-usuario-texto">
                            <span className="parcela-nombre">{cuenta.nombre || cuenta.usuario}</span>
                            <span className="parcela-meta">
                              @{cuenta.usuario}
                              {cuenta.telefono ? ` · ${cuenta.telefono}` : ''}
                            </span>
                          </span>
                        </span>
                        <span className="parcela-meta">
                          Último inicio: {cuenta.ultimo_inicio ? textoRelativo(cuenta.ultimo_inicio) : 'nunca'}
                        </span>
                        <span className="parcela-chips">
                          <Insignia
                            tono={cuenta.activo ? 'ok' : 'neutro'}
                            texto={cuenta.activo ? 'Activa' : 'Desactivada'}
                          />
                          <span className="chip">{cuenta.parcelas} parcelas</span>
                          <span className="chip">{cuenta.reportes} reportes</span>
                        </span>
                      </button>
                    </li>
                  )
                })}
              </ul>
            </>
          )}
        </aside>

        <div className="detalle">
          {usuarioId ? (
            <>
              <button type="button" className="boton secundario solo-movil" onClick={onVolver}>
                <Icono nombre="atras" tamano={16} />
                Volver a los usuarios
              </button>
              <UsuarioDetalle key={usuarioId} usuarioId={usuarioId} onCambio={cuentas.recargar} />
            </>
          ) : (
            <EstadoVacio icono="persona" titulo="Elige un usuario">
              Verás cuándo entró a la app, sus parcelas y sus últimos reportes.
            </EstadoVacio>
          )}
        </div>
      </div>
    </>
  )
}
