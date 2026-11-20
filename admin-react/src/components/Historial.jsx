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

export default function Historial({ parcelaId }) {
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [intento, setIntento] = useState(0)
  const [resultado, setResultado] = useState({ clave: null })

  const clave = `${parcelaId}|${desde}|${hasta}|${intento}`

  useEffect(() => {
    let vigente = true
    obtenerHistorial(parcelaId, { desde, hasta })
      .then((datos) => vigente && setResultado({ clave, datos }))
      .catch((error) => vigente && setResultado({ clave, error }))
    return () => {
      vigente = false
    }
  }, [clave, parcelaId, desde, hasta])

  const cargando = resultado.clave !== clave
  const { datos, error } = resultado
  const reintentar = () => setIntento((n) => n + 1)
  const rangoInvalido = desde && hasta && desde > hasta

  return (
    <section className="panel">
      <h2>{datos && !cargando ? datos.parcela.nombre : 'Historial'}</h2>

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
          <AvisoAcceso mensaje={error.message} onReintentar={reintentar} />
        ) : (
          <div className="aviso">
            <p>{error.message}</p>
            <button type="button" className="boton" onClick={reintentar}>
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
                return (
                  <li key={d.id} className={`evento sev-${nivel.clave}`}>
                    <span className="punto" />
                    <div className="encabezado">
                      <strong>{d.plaga_nombre || nombrePlaga(d.plaga_detectada)}</strong>
                      <span className={`etiqueta sev-${nivel.clave}`}>{nivel.texto}</span>
                    </div>
                    <p className="muted">
                      {formatearFecha(d.fecha_captura)} · Confianza {formatearConfianza(d.confianza)}
                    </p>
                    {d.recomendacion && <p>{d.recomendacion}</p>}
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