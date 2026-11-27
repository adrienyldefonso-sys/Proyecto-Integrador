import { obtenerAlertas } from '../api'
import { URGENCIAS, formatearConfianza, nombrePlaga, porcentaje, textoRelativo } from '../formato'
import { useCarga } from '../hooks/useCarga'
import EncabezadoPagina from './EncabezadoPagina'
import ErrorCarga from './ErrorCarga'
import EstadoVacio from './EstadoVacio'
import Esqueleto from './Esqueleto'
import Icono from './Icono'
import Insignia from './Insignia'
import MetaParcela from './MetaParcela'

const REFRESCO_MS = 60000
const NIVELES = ['ALTA', 'MEDIA', 'SIN_ALERTA']
const ICONOS_KPI = { ALTA: 'alerta', MEDIA: 'alerta', SIN_ALERTA: 'ok' }

function Cargando() {
  return (
    <div role="status" aria-label="Cargando alertas" className="tablero-cargando">
      <div className="kpis">
        {NIVELES.map((nivel) => (
          <div key={nivel} className="kpi">
            <Esqueleto ancho="48px" alto="48px" radio="14px" />
            <div className="kpi-esqueleto">
              <Esqueleto ancho="3rem" alto="1.8rem" />
              <Esqueleto ancho="7rem" alto="1rem" />
            </div>
          </div>
        ))}
      </div>
      <div className="tarjeta-panel">
        {[0, 1, 2].map((i) => (
          <Esqueleto key={i} alto="110px" radio="16px" className="bloque" />
        ))}
      </div>
    </div>
  )
}

function Visita({ item, onVerHistorial }) {
  const urgencia = URGENCIAS[item.nivel]
  const ultimo = item.ultimo_diagnostico

  return (
    <article className={`visita visita-${urgencia.tono}`}>
      <span className="numero" aria-label={`Visita número ${item.orden}`}>
        {item.orden}
      </span>
      <div className="visita-cuerpo">
        <div className="visita-titulo">
          <h3>{item.parcela.nombre}</h3>
          <Insignia tono={urgencia.tono} texto={urgencia.texto} />
        </div>
        <MetaParcela parcela={item.parcela} />
        <p className="motivo">{item.motivo}</p>
        {ultimo && (
          <p className="ultimo">
            <Icono nombre="reloj" tamano={14} />
            {ultimo.plaga_nombre || nombrePlaga(ultimo.plaga_detectada)} · {textoRelativo(ultimo.fecha_captura)} ·
            Confianza {formatearConfianza(ultimo.confianza)}
          </p>
        )}
        <div className="visita-acciones">
          <button
            type="button"
            className="boton secundario pequeno"
            onClick={() => onVerHistorial(item.parcela.id)}
          >
            Ver historial
            <Icono nombre="flecha" tamano={16} />
          </button>
        </div>
      </div>
    </article>
  )
}

function BarraEstado({ resumen }) {
  const total = resumen.total
  const descripcion = NIVELES.map((nivel) => `${URGENCIAS[nivel].texto}: ${resumen[nivel]}`).join(', ')

  return (
    <>
      <div className="barra-estado" role="img" aria-label={descripcion}>
        {NIVELES.filter((nivel) => resumen[nivel] > 0).map((nivel) => (
          <span
            key={nivel}
            className={`tramo tramo-${URGENCIAS[nivel].tono}`}
            style={{ flexGrow: resumen[nivel] }}
          >
            {porcentaje(resumen[nivel], total) >= 12 && (
              <b className="tramo-valor">{resumen[nivel]}</b>
            )}
          </span>
        ))}
        {total === 0 && <span className="tramo tramo-neutro" style={{ flexGrow: 1 }} />}
      </div>
      <ul className="leyenda">
        {NIVELES.map((nivel) => (
          <li key={nivel}>
            <span className={`punto-leyenda punto-${URGENCIAS[nivel].tono}`} />
            <span>{URGENCIAS[nivel].texto}</span>
            <strong>
              {resumen[nivel]} · {porcentaje(resumen[nivel], total)} %
            </strong>
          </li>
        ))}
      </ul>
    </>
  )
}

export default function Tablero({ onVerHistorial }) {
  const { datos, error, cargando, actualizado, recargar } = useCarga(obtenerAlertas, [], {
    refrescoMs: REFRESCO_MS,
  })

  const cabecera = (
    <EncabezadoPagina
      titulo="Alertas y ruta"
      subtitulo="Prioriza tus visitas técnicas según el último diagnóstico de cada parcela."
      actualizado={actualizado}
      onActualizar={recargar}
    />
  )

  if (error) {
    return (
      <>
        {cabecera}
        <ErrorCarga error={error} titulo="No se pudo cargar el tablero" onReintentar={recargar} />
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

  const { resumen, grupos } = datos
  const ruta = [...grupos.ALTA, ...grupos.MEDIA]
  const sinAlerta = grupos.SIN_ALERTA

  return (
    <>
      {cabecera}

      <section className="kpis" aria-label="Parcelas por nivel de urgencia">
        {NIVELES.map((nivel) => (
          <article key={nivel} className={`kpi kpi-${URGENCIAS[nivel].tono}`}>
            <span className="kpi-icono">
              <Icono nombre={ICONOS_KPI[nivel]} tamano={24} />
            </span>
            <div>
              <p className="kpi-valor">{resumen[nivel]}</p>
              <p className="kpi-etiqueta">{URGENCIAS[nivel].texto}</p>
            </div>
          </article>
        ))}
      </section>

      <div className="dos-columnas">
        <section className="tarjeta-panel" aria-labelledby="titulo-ruta">
          <div className="barra">
            <h2 id="titulo-ruta">Ruta de visitas sugerida</h2>
            <span className="muted">
              {ruta.length === 1 ? '1 parcela por visitar' : `${ruta.length} parcelas por visitar`}
            </span>
          </div>

          {ruta.length === 0 ? (
            <EstadoVacio icono="ok" titulo="Sin visitas urgentes">
              Ninguna parcela necesita una visita por ahora.
            </EstadoVacio>
          ) : (
            <ol className="ruta">
              {ruta.map((item) => (
                <li key={item.parcela.id}>
                  <Visita item={item} onVerHistorial={onVerHistorial} />
                </li>
              ))}
            </ol>
          )}

          {sinAlerta.length > 0 && (
            <details className="sin-alerta">
              <summary>Sin alerta ({sinAlerta.length})</summary>
              <ul className="chips-lista">
                {sinAlerta.map((item) => (
                  <li key={item.parcela.id}>
                    <button type="button" className="parcela-simple" onClick={() => onVerHistorial(item.parcela.id)}>
                      <strong>{item.parcela.nombre}</strong>
                      <span className="muted">{item.motivo}</span>
                    </button>
                  </li>
                ))}
              </ul>
            </details>
          )}
        </section>

        <aside className="lateral">
          <section className="tarjeta-panel">
            <h2>Estado de las parcelas</h2>
            <BarraEstado resumen={resumen} />
          </section>

          <section className="tarjeta-panel ayuda">
            <h2>¿Cómo se calcula la urgencia?</h2>
            <ul>
              <li>
                <Insignia tono="alta" texto="Alta" />
                <span>El último diagnóstico es de severidad alta.</span>
              </li>
              <li>
                <Insignia tono="media" texto="Media" />
                <span>El último es de severidad media, o hay 2 o más medias en 15 días.</span>
              </li>
              <li>
                <Insignia tono="ok" texto="Sin alerta" />
                <span>Cualquier otro caso.</span>
              </li>
            </ul>
            <p className="muted">Dentro de cada nivel va primero el diagnóstico más reciente.</p>
          </section>
        </aside>
      </div>
    </>
  )
}
