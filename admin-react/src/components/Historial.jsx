import { useEffect, useState } from 'react'
import { esSinAcceso, obtenerHistorial } from '../api'
import {
  formatearConfianza,
  formatearFecha,
  nivelSeveridad,
  nombrePlaga,
  textoConteo,
} from '../formato'
import AvisoAcceso from './AvisoAcceso'
import FotoEvidencia from './FotoEvidencia'

const REFRESCO_MS = 30000

export default function Historial({ parcelaId }) {
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [version, setVersion] = useState(0)
  const [resultado, setResultado] = useState({ clave: null })
  const [actualizado, setActualizado] = useState(null)

  // La clave solo cambia con la parcela y los filtros: un refresco no vuelve a mostrar "Cargando"
  const clave = `${parcelaId}|${desde}|${hasta}`

  useEffect(() => {
    let vigente = true
    obtenerHistorial(parcelaId, { desde, hasta })
      .then((datos) => {
        if (!vigente) return
        setResultado({ clave, datos })
        setActualizado(new Date())
      })
      .catch((error) => {
        if (!vigente) return
        // Si falla un refresco silencioso, se conserva lo que ya se estaba mostrando
        setResultado((previo) =>
          previo.clave === clave && previo.datos ? previo : { clave, error },
        )
      })
    return () => {
      vigente = false
    }
  }, [clave, parcelaId, desde, hasta, version])

  useEffect(() => {
    const temporizador = setInterval(() => {
      if (!document.hidden) setVersion((n) => n + 1)
    }, REFRESCO_MS)
    return () => clearInterval(temporizador)
  }, [])

  const cargando = resultado.clave !== clave
  const { datos, error } = resultado
  const actualizar = () => setVersion((n) => n + 1)
  const rangoInvalido = desde && hasta && desde > hasta

  return (
    <section className="panel">
      <div className="barra">
        <h2>{datos && !cargando ? datos.parcela.nombre : 'Historial'}</h2>
        <div className="acciones">
          {actualizado && (
            <span className="muted">
              Actualizado a las{' '}
              {actualizado.toLocaleTimeString('es-PE', {
                hour: '2-digit',
                minute: '2-digit',
                timeZone: 'America/Lima',
              })}
            </span>
          )}
          <button type="button" className="boton secundario" onClick={actualizar}>
            Actualizar
          </button>
        </div>
      </div>

      <div className="filtros">
        <label>
          Desde
          <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
        </label>
        <label>
          Hasta
          <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
        </label>
        {(desde || hasta) && (
          <button
            type="button"
            className="boton secundario"
            onClick={() => {
              setDesde('')
              setHasta('')
            }}
          >
            Quitar filtros
          </button>
        )}
      </div>
      {rangoInvalido && <p className="alerta">La fecha inicial es posterior a la final.</p>}

      {cargando && <p className="muted">Cargando historial...</p>}

      {!cargando && error &&
        (esSinAcceso(error) ? (
          <AvisoAcceso mensaje={error.message} onReintentar={actualizar} />
        ) : (
          <div className="aviso">
            <p>{error.message}</p>
            <button type="button" className="boton" onClick={actualizar}>
              Reintentar
            </button>
          </div>
        ))}

      {!cargando && datos && (
        <>
          <p className="muted">
            {datos.parcela.cultivo}
            {datos.parcela.referencia ? ` · ${datos.parcela.referencia}` : ''} ·{' '}
            {datos.parcela.agricultor}
          </p>

          <div className="resumen">
            <span className="chip">{textoConteo(datos.resumen.total)}</span>
            {datos.resumen.ultima_fecha && (
              <span className="chip">Último: {formatearFecha(datos.resumen.ultima_fecha)}</span>
            )}
            {Object.entries(datos.resumen.por_plaga).map(([plaga, total]) => (
              <span key={plaga} className="chip">
                {nombrePlaga(plaga)}: {total}
              </span>
            ))}
          </div>

          {datos.diagnosticos.length === 0 ? (
            <p className="muted">No hay diagnósticos en este periodo.</p>
          ) : (
            <ol className="linea">
              {datos.diagnosticos.map((d) => {
                const nivel = nivelSeveridad(d.nivel_severidad)
                const plaga = d.plaga_nombre || nombrePlaga(d.plaga_detectada)
                return (
                  <li key={d.id} className={`evento sev-${nivel.clave}`}>
                    <span className="punto" />
                    <div className="encabezado">
                      <strong>{plaga}</strong>
                      <span className={`etiqueta sev-${nivel.clave}`}>{nivel.texto}</span>
                    </div>
                    <p className="muted">
                      {formatearFecha(d.fecha_captura)} · Confianza{' '}
                      {formatearConfianza(d.confianza)}
                    </p>
                    {d.recomendacion && <p>{d.recomendacion}</p>}
                    {d.estado_revision && d.estado_revision !== 'NINGUNA' && (
                      <p>
                        <span className="chip">
                          {d.estado_revision === 'SOLICITADA' && 'Revisión solicitada'}
                          {d.estado_revision === 'CONFIRMADA' && 'Confirmado por agrónomo'}
                          {d.estado_revision === 'CORREGIDA' &&
                            `Corregido por agrónomo: ${d.etiqueta_validada_nombre}`}
                        </span>
                      </p>
                    )}
                    {d.observacion_agronomo && <p className="muted">“{d.observacion_agronomo}”</p>}
                    <FotoEvidencia
                      url={d.foto_url}
                      descripcion={`${plaga} · ${formatearFecha(d.fecha_captura)}`}
                    />
                  </li>
                )
              })}
            </ol>
          )}
        </>
      )}
    </section>
  )
}