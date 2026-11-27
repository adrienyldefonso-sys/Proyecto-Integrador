import { obtenerMetricas } from '../api'
import { URGENCIAS, formatearConfianza, formatearHora, nombrePlaga, porcentaje, textoRelativo } from '../formato'
import { useCarga } from '../hooks/useCarga'
import EncabezadoPagina from './EncabezadoPagina'
import Icono from './Icono'
import ErrorCarga from './ErrorCarga'
import Esqueleto from './Esqueleto'

const REFRESCO_MS = 60000
const NIVELES = ['ALTA', 'MEDIA', 'SIN_ALERTA']
// Cada diagnóstico tiene su color fijo en todo el resumen: verde sano, ámbar medio, naranja alto
const TONO_PLAGA = { Potato___healthy: 'ok', Potato___Early_blight: 'media', Potato___Late_blight: 'alta' }

const ICONOS_INDICADOR = ['parcela', 'alerta', 'reloj', 'persona', 'revisar', 'ok']

// Paisaje de cabecera: colinas y surcos dibujados en SVG, sin imágenes externas
function Paisaje() {
  return (
    <svg className="resumen-paisaje" viewBox="0 0 1200 220" preserveAspectRatio="xMidYMax slice" aria-hidden="true" focusable="false">
      <circle cx="1010" cy="62" r="30" className="paisaje-sol" />
      <path d="M0 128 C 160 84, 330 104, 500 132 S 860 92, 1200 124 L1200 220 L0 220 Z" className="paisaje-colina-lejana" />
      <path d="M0 168 C 220 124, 420 152, 620 170 S 980 134, 1200 160 L1200 220 L0 220 Z" className="paisaje-colina" />
      {[0, 1, 2, 3].map((i) => (
        <path key={i} d={`M0 ${190 + i * 8} Q 600 ${182 + i * 8} 1200 ${188 + i * 8}`} className="paisaje-surco" />
      ))}
    </svg>
  )
}

const etiquetaSemana = (iso) =>
  new Date(`${iso}T00:00:00`).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' })

function Cargando() {
  return (
    <div role="status" aria-label="Cargando resumen" className="resumen-cargando">
      <Esqueleto alto="120px" radio="16px" className="bloque" />
      <Esqueleto alto="280px" radio="16px" className="bloque" />
    </div>
  )
}

export default function Resumen({ onIr }) {
  const { datos, error, cargando, actualizado, recargar } = useCarga(obtenerMetricas, [], {
    refrescoMs: REFRESCO_MS,
  })

  const cabecera = (
    <EncabezadoPagina
      titulo="Resumen general"
      subtitulo="Indicadores del monitoreo de parcelas. Se actualiza cada minuto."
      actualizado={actualizado}
      onActualizar={recargar}
    />
  )

  if (error) {
    return (
      <>
        {cabecera}
        <ErrorCarga error={error} titulo="No se pudo cargar el resumen" onReintentar={recargar} />
      </>
    )
  }

  if (cargando || !datos) {
    return (
      <>
        {cabecera}
        <Cargando />
      </>
    )
  }

  const { kpis, urgencia, plagas, semanas, recientes } = datos
  const totalDiagnosticos = plagas.reduce((suma, p) => suma + p.total, 0)
  const maximoSemana = Math.max(0, ...semanas.map((s) => s.total))

  const indicadores = [
    { etiqueta: 'Parcelas registradas', valor: kpis.parcelas },
    { etiqueta: 'Con urgencia alta', valor: kpis.alerta_alta, tono: 'alta' },
    { etiqueta: 'Diagnósticos en 30 días', valor: kpis.diagnosticos_recientes },
    { etiqueta: 'Agricultores activos', valor: kpis.agricultores_activos },
    { etiqueta: 'Revisiones pendientes', valor: kpis.revisiones_pendientes, tono: 'media' },
    {
      etiqueta: 'Confianza promedio',
      valor: kpis.confianza_promedio == null ? '—' : formatearConfianza(kpis.confianza_promedio),
    },
  ]

  return (
    <div className="resumen">
      <header className="resumen-hero">
        <Paisaje />
        <div className="resumen-hero-texto">
          <p className="resumen-kicker">Monitoreo agrícola</p>
          <h1 className="resumen-titulo">Resumen general</h1>
          <p className="resumen-sub">Estado de las parcelas y actividad del panel, actualizado cada minuto.</p>
        </div>
        <div className="resumen-hero-acciones">
          {actualizado && <span>Actualizado a las {formatearHora(actualizado)}</span>}
          <button type="button" className="boton" onClick={recargar}>
            <Icono nombre="actualizar" tamano={16} />
            Actualizar
          </button>
        </div>
      </header>

      <section className="resumen-indicadores" aria-label="Indicadores clave">
        {indicadores.map((indicador, indice) => (
          <article key={indicador.etiqueta} className={`indicador${indicador.tono ? ` indicador-${indicador.tono}` : ''}`}>
            <span className="indicador-icono">
              <Icono nombre={ICONOS_INDICADOR[indice]} tamano={18} />
            </span>
            <p className="indicador-etiqueta">{indicador.etiqueta}</p>
            <p className="indicador-valor">{indicador.valor}</p>
          </article>
        ))}
      </section>

      <nav className="resumen-accesos" aria-label="Accesos rápidos">
        <button type="button" className="boton secundario pequeno" onClick={() => onIr('alertas')}>
          Ver alertas y ruta
        </button>
        <button type="button" className="boton secundario pequeno" onClick={() => onIr('revisiones')}>
          Revisar diagnósticos
        </button>
        <button type="button" className="boton secundario pequeno" onClick={() => onIr('usuarios')}>
          Gestionar usuarios
        </button>
      </nav>

      <div className="resumen-grid">
        <section className="tarjeta-panel panel-ancho" aria-labelledby="titulo-semanas">
          <h2 id="titulo-semanas">Diagnósticos por semana</h2>
          <p className="muted">Últimas 8 semanas, de lunes a domingo.</p>
          <ol className="columnas" aria-label="Diagnósticos por semana">
            {semanas.map((semana) => (
              <li key={semana.inicio} className="columna">
                <span className="columna-valor">{semana.total}</span>
                <span
                  className="columna-barra"
                  style={{ height: maximoSemana ? `${(semana.total / maximoSemana) * 100}%` : '0%' }}
                />
                <span className="columna-etiqueta">{etiquetaSemana(semana.inicio)}</span>
              </li>
            ))}
          </ol>
        </section>

        <section className="tarjeta-panel" aria-labelledby="titulo-urgencia">
          <h2 id="titulo-urgencia">Parcelas por urgencia</h2>
          <p className="muted">Nivel calculado con el último diagnóstico de cada parcela.</p>
          <ul className="barras-h">
            {NIVELES.map((nivel) => (
              <li key={nivel}>
                <div className="barras-h-cabecera">
                  <span>{URGENCIAS[nivel].texto}</span>
                  <strong>
                    {urgencia[nivel]} · {porcentaje(urgencia[nivel], kpis.parcelas)} %
                  </strong>
                </div>
                <div className="barras-h-pista" aria-hidden="true">
                  <span
                    className={`barras-h-relleno barras-${URGENCIAS[nivel].tono}`}
                    style={{ width: `${porcentaje(urgencia[nivel], kpis.parcelas)}%` }}
                  />
                </div>
              </li>
            ))}
          </ul>
        </section>

        <section className="tarjeta-panel" aria-labelledby="titulo-plagas">
          <h2 id="titulo-plagas">Distribución por plaga</h2>
          {totalDiagnosticos === 0 ? (
            <p className="muted">Aún no hay diagnósticos registrados.</p>
          ) : (
            <table className="tabla-resumen">
              <thead>
                <tr>
                  <th scope="col">Diagnóstico</th>
                  <th scope="col" className="numerico">Casos</th>
                  <th scope="col">Participación</th>
                </tr>
              </thead>
              <tbody>
                {plagas.map((plaga) => (
                  <tr key={plaga.clave}>
                    <td>
                      <span className={`punto-plaga punto-${TONO_PLAGA[plaga.clave] ?? 'ok'}`} aria-hidden="true" />
                      {nombrePlaga(plaga.clave)}
                    </td>
                    <td className="numerico">{plaga.total}</td>
                    <td>
                      <div className="participacion">
                        <span className="participacion-pista" aria-hidden="true">
                          <span
                            className={`participacion-relleno barras-${TONO_PLAGA[plaga.clave] ?? 'ok'}`}
                            style={{ width: `${porcentaje(plaga.total, totalDiagnosticos)}%` }}
                          />
                        </span>
                        <span className="numerico">{porcentaje(plaga.total, totalDiagnosticos)} %</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <section className="tarjeta-panel panel-ancho" aria-labelledby="titulo-recientes">
          <h2 id="titulo-recientes">Diagnósticos recientes</h2>
          {recientes.length === 0 ? (
            <p className="muted">Aún no hay diagnósticos registrados.</p>
          ) : (
            <table className="tabla-resumen">
              <thead>
                <tr>
                  <th scope="col">Cuándo</th>
                  <th scope="col">Parcela</th>
                  <th scope="col">Diagnóstico</th>
                  <th scope="col" className="numerico">Confianza</th>
                </tr>
              </thead>
              <tbody>
                {recientes.map((diagnostico) => (
                  <tr key={diagnostico.id}>
                    <td>{textoRelativo(diagnostico.fecha)}</td>
                    <td>
                      {diagnostico.parcela_id ? (
                        <button
                          type="button"
                          className="enlace-tabla"
                          onClick={() => onIr('parcelas', diagnostico.parcela_id)}
                        >
                          {diagnostico.parcela}
                        </button>
                      ) : (
                        <span className="muted">Sin parcela</span>
                      )}
                    </td>
                    <td>
                      <span className={`punto-plaga punto-${TONO_PLAGA[diagnostico.plaga] ?? 'ok'}`} aria-hidden="true" />
                      {nombrePlaga(diagnostico.plaga)}
                    </td>
                    <td className="numerico">{formatearConfianza(diagnostico.confianza)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  )
}
