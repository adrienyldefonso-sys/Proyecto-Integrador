import { useEffect, useState } from 'react'
import { esSinAcceso, obtenerAlertas } from '../api'
import { formatearConfianza, formatearFecha, nombrePlaga } from '../formato'
import AvisoAcceso from './AvisoAcceso'

const NIVELES = {
  ALTA: { titulo: 'Urgencia alta', clase: 'sev-ALTA' },
  MEDIA: { titulo: 'Urgencia media', clase: 'sev-MEDIA' },
  SIN_ALERTA: { titulo: 'Sin alerta', clase: 'sev-NINGUNA' },
}

function detalleParcela(parcela) {
  const lugar = parcela.referencia ? ` · ${parcela.referencia}` : ''
  return `${parcela.cultivo}${lugar} · ${parcela.agricultor}`
}

export default function Tablero({ onVerHistorial }) {
  const [intento, setIntento] = useState(0)
  const [resultado, setResultado] = useState({ intento: null })

  useEffect(() => {
    let vigente = true
    obtenerAlertas()
      .then((datos) => {
        if (vigente) setResultado({ intento, datos })
      })
      .catch((error) => {
        if (vigente) setResultado({ intento, error })
      })
    return () => {
      vigente = false
    }
  }, [intento])

  const cargando = resultado.intento !== intento
  const { datos, error } = resultado
  const actualizar = () => setIntento((n) => n + 1)

  if (cargando) {
    return (
      <section className="panel">
        <p className="muted">Cargando alertas...</p>
      </section>
    )
  }

  if (error) {
    return esSinAcceso(error) ? (
      <AvisoAcceso mensaje={error.message} onReintentar={actualizar} />
    ) : (
      <section className="aviso">
        <h3>No se pudo cargar el tablero</h3>
        <p>{error.message}</p>
        <button type="button" className="boton" onClick={actualizar}>
          Reintentar
        </button>
      </section>
    )
  }

  const ruta = [...datos.grupos.ALTA, ...datos.grupos.MEDIA]
  const sinAlerta = datos.grupos.SIN_ALERTA

  return (
    <section className="panel">
      <div className="barra">
        <h2>Tablero de alertas</h2>
        <button type="button" className="boton secundario" onClick={actualizar}>
          Actualizar
        </button>
      </div>

      <div className="resumen-tarjetas">
        {Object.entries(NIVELES).map(([clave, nivel]) => (
          <div key={clave} className={`conteo ${nivel.clase}`}>
            <strong>{datos.resumen[clave]}</strong>
            <span>{nivel.titulo}</span>
          </div>
        ))}
      </div>

      <h3>Ruta de visitas</h3>
      {ruta.length === 0 ? (
        <p className="muted">No hay parcelas con alerta por visitar por ahora.</p>
      ) : (
        <ol className="ruta">
          {ruta.map((item) => {
            const nivel = NIVELES[item.nivel]
            const ultimo = item.ultimo_diagnostico
            return (
              <li key={item.parcela.id} className={`visita ${nivel.clase}`}>
                <span className="numero">{item.orden}</span>
                <div className="visita-cuerpo">
                  <div className="encabezado">
                    <strong>{item.parcela.nombre}</strong>
                    <span className={`etiqueta ${nivel.clase}`}>{nivel.titulo}</span>
                  </div>
                  <p className="muted">{detalleParcela(item.parcela)}</p>
                  <p>{item.motivo}</p>
                  {ultimo && (
                    <p className="muted">
                      Último: {ultimo.plaga_nombre || nombrePlaga(ultimo.plaga_detectada)} ·{' '}
                      {formatearFecha(ultimo.fecha_captura)} · Confianza{' '}
                      {formatearConfianza(ultimo.confianza)}
                    </p>
                  )}
                  <div>
                    <button
                      type="button"
                      className="boton secundario"
                      onClick={() => onVerHistorial(item.parcela.id)}
                    >
                      Ver historial
                    </button>
                  </div>
                </div>
              </li>
            )
          })}
        </ol>
      )}

      {sinAlerta.length > 0 && (
        <details className="sin-alerta">
          <summary>Sin alerta ({sinAlerta.length})</summary>
          <ul className="lista">
            {sinAlerta.map((item) => (
              <li key={item.parcela.id}>
                <button
                  type="button"
                  className="tarjeta"
                  onClick={() => onVerHistorial(item.parcela.id)}
                >
                  <strong>{item.parcela.nombre}</strong>
                  <span className="muted">{detalleParcela(item.parcela)}</span>
                  <span className="muted">{item.motivo}</span>
                </button>
              </li>
            ))}
          </ul>
        </details>
      )}
    </section>
  )
}