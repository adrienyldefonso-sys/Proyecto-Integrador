import { useState } from 'react'
import { obtenerHistorial } from '../api'
import {
  distribucionPlagas,
  estadoRevision,
  formatearConfianza,
  formatearFecha,
  formatearHora,
  nivelSeveridad,
  nombrePlaga,
  rangoRapido,
  textoRelativo,
  tonoDe,
} from '../formato'
import { useCarga } from '../hooks/useCarga'
import ErrorCarga from './ErrorCarga'
import EstadoVacio from './EstadoVacio'
import Esqueleto from './Esqueleto'
import FotoEvidencia from './FotoEvidencia'
import Icono from './Icono'
import Insignia from './Insignia'
import MetaParcela from './MetaParcela'

const REFRESCO_MS = 30000

const RANGOS = [
  { dias: 7, texto: '7 días' },
  { dias: 30, texto: '30 días' },
  { dias: null, texto: 'Todo' },
]

function CargandoHistorial() {
  return (
    <div role="status" aria-label="Cargando historial" className="historial-cargando">
      <div className="stats">
        {[0, 1, 2].map((i) => (
          <div key={i} className="stat">
            <Esqueleto ancho="6rem" alto="0.9rem" />
            <Esqueleto ancho="4rem" alto="1.8rem" />
          </div>
        ))}
      </div>
      {[0, 1].map((i) => (
        <Esqueleto key={i} alto="150px" radio="16px" className="bloque" />
      ))}
    </div>
  )
}

function Estadisticas({ resumen, distribucion }) {
  const ultima = resumen.ultima_fecha
  const masFrecuente = distribucion[0]

  return (
    <div className="stats">
      <div className="stat">
        <span className="stat-etiqueta">Diagnósticos</span>
        <strong className="stat-valor">{resumen.total}</strong>
      </div>
      <div className="stat">
        <span className="stat-etiqueta">Último diagnóstico</span>
        <strong className="stat-valor stat-texto">{ultima ? textoRelativo(ultima) : '—'}</strong>
        <span className="muted">{ultima ? formatearFecha(ultima) : 'Sin registros'}</span>
      </div>
      <div className="stat">
        <span className="stat-etiqueta">Más frecuente</span>
        <strong className="stat-valor stat-texto">{masFrecuente ? masFrecuente.nombre : '—'}</strong>
        <span className="muted">
          {masFrecuente ? `${masFrecuente.total} de ${resumen.total}` : 'Sin registros'}
        </span>
      </div>
    </div>
  )
}

function Evento({ d }) {
  const nivel = nivelSeveridad(d.nivel_severidad)
  const tono = tonoDe(nivel.clave)
  const plaga = d.plaga_nombre || nombrePlaga(d.plaga_detectada)
  const revision = estadoRevision(d.estado_revision)
  const confianza = Math.min(100, Math.max(0, Number(d.confianza) || 0))
  const textoRevision =
    revision && d.estado_revision === 'CORREGIDA' && d.etiqueta_validada_nombre
      ? `${revision.texto}: ${d.etiqueta_validada_nombre}`
      : revision?.texto

  return (
    <li className={`evento evento-${tono}`}>
      <span className="evento-punto" aria-hidden="true" />
      <article className="evento-tarjeta">
        <FotoEvidencia url={d.foto_url} descripcion={`${plaga} · ${formatearFecha(d.fecha_captura)}`} />
        <div className="evento-cuerpo">
          <div className="evento-titulo">
            <h3>{plaga}</h3>
            <Insignia tono={tono} texto={nivel.texto} />
          </div>
          <p className="meta">
            <span>
              <Icono nombre="calendario" tamano={14} />
              {formatearFecha(d.fecha_captura)}
            </span>
            <span>{textoRelativo(d.fecha_captura)}</span>
          </p>
          <div className="confianza">
            <span>Confianza {formatearConfianza(d.confianza)}</span>
            <span className="barra-fina" aria-hidden="true">
              <span style={{ width: `${confianza}%` }} />
            </span>
          </div>
          {d.recomendacion && (
            <div className="recomendacion">
              <strong>Recomendación</strong>
              <p>{d.recomendacion}</p>
            </div>
          )}
          {revision && (
            <div className="evento-revision">
              <Insignia tono={revision.tono} texto={textoRevision} />
            </div>
          )}
          {d.observacion_agronomo && <p className="observacion">“{d.observacion_agronomo}”</p>}
        </div>
      </article>
    </li>
  )
}

export default function Historial({ parcelaId }) {
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const rangoInvalido = Boolean(desde && hasta && desde > hasta)

  const { datos, error, cargando, actualizado, recargar } = useCarga(
    () => obtenerHistorial(parcelaId, { desde, hasta }),
    [parcelaId, desde, hasta],
    { refrescoMs: REFRESCO_MS },
  )

  const aplicarRango = (dias) => {
    if (dias === null) {
      setDesde('')
      setHasta('')
      return
    }
    const rango = rangoRapido(dias)
    setDesde(rango.desde)
    setHasta(rango.hasta)
  }

  const rangoActivo = (dias) => {
    if (dias === null) return !desde && !hasta
    const rango = rangoRapido(dias)
    return desde === rango.desde && hasta === rango.hasta
  }

  const distribucion = datos ? distribucionPlagas(datos.resumen.por_plaga) : []

  return (
    <section className="historial" aria-label="Historial de la parcela">
      <header className="tarjeta-panel historial-cab">
        <div className="historial-titulo">
          <h2>{datos ? datos.parcela.nombre : <Esqueleto ancho="12rem" alto="1.6rem" />}</h2>
          {datos && <MetaParcela parcela={datos.parcela} />}
        </div>
        <div className="pagina-acciones">
          {actualizado && <span className="muted">Actualizado a las {formatearHora(actualizado)}</span>}
          <button type="button" className="boton secundario" onClick={recargar}>
            <Icono nombre="actualizar" tamano={16} />
            Actualizar
          </button>
        </div>
      </header>

      <div className="tarjeta-panel filtros">
        <div className="filtros-rapidos" role="group" aria-label="Periodo">
          {RANGOS.map((rango) => (
            <button
              key={rango.texto}
              type="button"
              className={`chip-boton${rangoActivo(rango.dias) ? ' activa' : ''}`}
              aria-pressed={rangoActivo(rango.dias)}
              onClick={() => aplicarRango(rango.dias)}
            >
              {rango.texto}
            </button>
          ))}
        </div>
        <label>
          Desde
          <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
        </label>
        <label>
          Hasta
          <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
        </label>
      </div>
      {rangoInvalido && (
        <p className="error-inline" role="alert">
          La fecha inicial es posterior a la final.
        </p>
      )}

      {error && <ErrorCarga error={error} titulo="No se pudo cargar el historial" onReintentar={recargar} />}
      {cargando && <CargandoHistorial />}

      {datos && (
        <>
          <Estadisticas resumen={datos.resumen} distribucion={distribucion} />

          {distribucion.length > 0 && (
            <section className="tarjeta-panel" aria-labelledby="titulo-distribucion">
              <h3 id="titulo-distribucion" className="titulo-seccion">
                Diagnósticos por tipo
              </h3>
              <ul className="distribucion">
                {distribucion.map((d) => (
                  <li key={d.clave}>
                    <span className="dist-nombre">{d.nombre}</span>
                    <span className="dist-barra" aria-hidden="true">
                      <span className={`dist-relleno relleno-${d.tono}`} style={{ width: `${d.porcentaje}%` }} />
                    </span>
                    <span className="dist-valor">
                      {d.total} · {d.porcentaje} %
                    </span>
                  </li>
                ))}
              </ul>
            </section>
          )}

          {datos.diagnosticos.length === 0 ? (
            <section className="tarjeta-panel">
              <EstadoVacio icono="calendario" titulo="Sin diagnósticos en este periodo">
                Prueba con otro rango de fechas.
              </EstadoVacio>
            </section>
          ) : (
            <ol className="linea">
              {datos.diagnosticos.map((d) => (
                <Evento key={d.id} d={d} />
              ))}
            </ol>
          )}
        </>
      )}
    </section>
  )
}
