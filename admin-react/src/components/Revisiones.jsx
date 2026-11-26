import { useEffect, useState } from 'react'
import { decidirRevision, esSinAcceso, obtenerRevisiones, obtenerSesion } from '../api'
import { formatearConfianza, formatearFecha, nombrePlaga } from '../formato'
import AvisoAcceso from './AvisoAcceso'
import FotoEvidencia from './FotoEvidencia'

const FILTROS = [
  ['pendientes', 'Pendientes'],
  ['revisadas', 'Revisadas'],
  ['todas', 'Todas'],
]

const PLAGAS = [
  ['Potato___Early_blight', 'Tizón temprano'],
  ['Potato___Late_blight', 'Tizón tardío'],
  ['Potato___healthy', 'Saludable'],
]

const ESTADOS = {
  SOLICITADA: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  CORREGIDA: 'Corregida',
}

export default function Revisiones() {
  const [filtro, setFiltro] = useState('pendientes')
  const [version, setVersion] = useState(0)
  const [resultado, setResultado] = useState({ clave: null })

  useEffect(() => {
    // Obtiene la cookie CSRF antes de que el agrónomo envíe una decisión
    obtenerSesion().catch(() => {})
  }, [])

  useEffect(() => {
    let vigente = true
    obtenerRevisiones(filtro)
      .then((datos) => {
        if (vigente) setResultado({ clave: filtro, datos })
      })
      .catch((error) => {
        if (vigente) setResultado({ clave: filtro, error })
      })
    return () => {
      vigente = false
    }
  }, [filtro, version])

  const cargando = resultado.clave !== filtro
  const { datos, error } = resultado
  const actualizar = () => setVersion((n) => n + 1)

  return (
    <section className="panel">
      <div className="barra">
        <h2>Revisiones de diagnóstico</h2>
        <div className="acciones">
          <button type="button" className="boton secundario" onClick={actualizar}>
            Actualizar
          </button>
        </div>
      </div>

      <div className="filtros">
        {FILTROS.map(([clave, texto]) => (
          <button
            key={clave}
            type="button"
            className={`chip${filtro === clave ? ' activa' : ''}`}
            onClick={() => setFiltro(clave)}
          >
            {texto}
          </button>
        ))}
      </div>

      {cargando && <p className="muted">Cargando revisiones...</p>}

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

      {!cargando && datos && datos.length === 0 && (
        <p className="muted">No hay diagnósticos en esta lista.</p>
      )}

      {!cargando &&
        datos &&
        datos.map((revision) => (
          <TarjetaRevision key={revision.id} revision={revision} onActualizado={actualizar} />
        ))}
    </section>
  )
}

function TarjetaRevision({ revision, onActualizado }) {
  const [editando, setEditando] = useState(false)
  const [etiqueta, setEtiqueta] = useState(
    PLAGAS.find(([clave]) => clave !== revision.plaga_detectada)?.[0] ?? PLAGAS[0][0],
  )
  const [observacion, setObservacion] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [mensajeError, setMensajeError] = useState('')

  const pendiente = revision.estado_revision === 'SOLICITADA'
  const plaga = revision.plaga_nombre || nombrePlaga(revision.plaga_detectada)

  const enviar = async (decision) => {
    setEnviando(true)
    setMensajeError('')
    try {
      await decidirRevision(revision.id, {
        decision,
        etiqueta_validada: decision === 'CORREGIR' ? etiqueta : '',
        observacion,
      })
      onActualizado()
    } catch (error) {
      setMensajeError(error.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <article className="evento">
      <div className="encabezado">
        <strong>{plaga}</strong>
        <span className="etiqueta">{ESTADOS[revision.estado_revision] ?? 'Sin revisión'}</span>
      </div>
      <p className="muted">
        {formatearFecha(revision.fecha_captura)} · Confianza {formatearConfianza(revision.confianza)}
        {' · '}
        {revision.parcela_nombre ?? 'Sin parcela'} · {revision.agricultor}
      </p>

      <FotoEvidencia url={revision.foto_url} descripcion={`${plaga} · ${formatearFecha(revision.fecha_captura)}`} />

      {revision.estado_revision === 'CORREGIDA' && (
        <p>
          <strong>Plaga validada:</strong> {revision.etiqueta_validada_nombre}
        </p>
      )}
      {revision.observacion_agronomo && <p className="muted">“{revision.observacion_agronomo}”</p>}

      {pendiente && (
        <div className="revision-acciones">
          {!editando ? (
            <>
              <button
                type="button"
                className="boton"
                disabled={enviando}
                onClick={() => enviar('CONFIRMAR')}
              >
                Confirmar diagnóstico
              </button>
              <button type="button" className="boton secundario" onClick={() => setEditando(true)}>
                Corregir
              </button>
            </>
          ) : (
            <div className="revision-form">
              <label>
                Plaga correcta
                <select value={etiqueta} onChange={(e) => setEtiqueta(e.target.value)}>
                  {PLAGAS.filter(([clave]) => clave !== revision.plaga_detectada).map(([clave, texto]) => (
                    <option key={clave} value={clave}>
                      {texto}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Observación para el agricultor
                <textarea
                  rows={3}
                  value={observacion}
                  onChange={(e) => setObservacion(e.target.value)}
                  placeholder="Por ejemplo: las manchas concéntricas indican tizón temprano."
                />
              </label>
              <div className="acciones">
                <button type="button" className="boton" disabled={enviando} onClick={() => enviar('CORREGIR')}>
                  Guardar corrección
                </button>
                <button type="button" className="boton secundario" onClick={() => setEditando(false)}>
                  Cancelar
                </button>
              </div>
            </div>
          )}
          {mensajeError && <p className="alerta">{mensajeError}</p>}
        </div>
      )}
    </article>
  )
}
