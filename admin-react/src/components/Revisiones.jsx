import { useEffect, useState } from 'react'
import { decidirRevision, obtenerRevisiones, obtenerSesion } from '../api'
import {
  OPCIONES_PLAGA,
  estadoRevision,
  formatearConfianza,
  formatearFecha,
  nombrePlaga,
  textoRelativo,
} from '../formato'
import { useCarga } from '../hooks/useCarga'
import EncabezadoPagina from './EncabezadoPagina'
import ErrorCarga from './ErrorCarga'
import EstadoVacio from './EstadoVacio'
import Esqueleto from './Esqueleto'
import FotoEvidencia from './FotoEvidencia'
import Icono from './Icono'
import Insignia from './Insignia'

const FILTROS = [
  ['pendientes', 'Pendientes'],
  ['revisadas', 'Revisadas'],
  ['todas', 'Todas'],
]

const MENSAJES_VACIO = {
  pendientes: ['ok', 'No hay revisiones pendientes', 'Cuando un agricultor pida revisar un diagnóstico, aparecerá aquí.'],
  revisadas: ['revisar', 'Aún no hay revisiones hechas', 'Los diagnósticos que confirmes o corrijas aparecerán aquí.'],
  todas: ['revisar', 'No hay diagnósticos en esta lista', 'Todavía no se ha pedido ninguna revisión.'],
}

function TarjetaRevision({ revision, onActualizado }) {
  const [editando, setEditando] = useState(false)
  const [etiqueta, setEtiqueta] = useState(
    OPCIONES_PLAGA.find((clave) => clave !== revision.plaga_detectada) ?? OPCIONES_PLAGA[0],
  )
  const [observacion, setObservacion] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [mensajeError, setMensajeError] = useState('')

  const estado = estadoRevision(revision.estado_revision)
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
    <article className="revision tarjeta-panel">
      <div className="revision-foto">
        <FotoEvidencia
          url={revision.foto_url}
          descripcion={`${plaga} · ${formatearFecha(revision.fecha_captura)}`}
        />
      </div>

      <div className="revision-cuerpo">
        <div className="evento-titulo">
          <h3>{plaga}</h3>
          {estado && <Insignia tono={estado.tono} texto={estado.corto} />}
        </div>

        <p className="meta">
          <span>
            <Icono nombre="calendario" tamano={14} />
            {formatearFecha(revision.fecha_captura)}
          </span>
          <span>{textoRelativo(revision.fecha_captura)}</span>
          <span>Confianza {formatearConfianza(revision.confianza)}</span>
        </p>
        <p className="meta">
          <span>
            <Icono nombre="parcela" tamano={14} />
            {revision.parcela_nombre ?? 'Sin parcela'}
          </span>
          <span>
            <Icono nombre="persona" tamano={14} />
            {revision.agricultor}
          </span>
        </p>

        {revision.estado_revision === 'CORREGIDA' && (
          <p className="validada">
            Plaga validada: <strong>{revision.etiqueta_validada_nombre}</strong>
          </p>
        )}
        {revision.observacion_agronomo && <p className="observacion">“{revision.observacion_agronomo}”</p>}

        {pendiente && (
          <div className="revision-acciones">
            {!editando ? (
              <div className="acciones">
                <button type="button" className="boton" disabled={enviando} onClick={() => enviar('CONFIRMAR')}>
                  <Icono nombre="ok" tamano={16} />
                  Confirmar diagnóstico
                </button>
                <button type="button" className="boton secundario" onClick={() => setEditando(true)}>
                  <Icono nombre="editar" tamano={16} />
                  Corregir
                </button>
              </div>
            ) : (
              <div className="revision-form">
                <label>
                  Plaga correcta
                  <select value={etiqueta} onChange={(e) => setEtiqueta(e.target.value)}>
                    {OPCIONES_PLAGA.filter((clave) => clave !== revision.plaga_detectada).map((clave) => (
                      <option key={clave} value={clave}>
                        {nombrePlaga(clave)}
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
            {mensajeError && (
              <p className="error-inline" role="alert">
                {mensajeError}
              </p>
            )}
          </div>
        )}
      </div>
    </article>
  )
}

export default function Revisiones() {
  const [filtro, setFiltro] = useState('pendientes')
  const { datos, error, cargando, actualizado, recargar } = useCarga(
    () => obtenerRevisiones(filtro),
    [filtro],
  )

  useEffect(() => {
    // Obtiene la cookie CSRF antes de que el agrónomo envíe una decisión
    obtenerSesion().catch(() => {})
  }, [])

  const [iconoVacio, tituloVacio, textoVacio] = MENSAJES_VACIO[filtro]

  return (
    <>
      <EncabezadoPagina
        titulo="Revisiones"
        subtitulo="Confirma o corrige los diagnósticos que los agricultores pidieron revisar."
        actualizado={actualizado}
        onActualizar={recargar}
      />

      <div className="segmentos" role="group" aria-label="Estado de la revisión">
        {FILTROS.map(([clave, texto]) => (
          <button
            key={clave}
            type="button"
            className={`segmento-btn${filtro === clave ? ' activa' : ''}`}
            aria-pressed={filtro === clave}
            onClick={() => setFiltro(clave)}
          >
            {texto}
          </button>
        ))}
      </div>

      {error && <ErrorCarga error={error} titulo="No se pudieron cargar las revisiones" onReintentar={recargar} />}

      {cargando && (
        <div role="status" aria-label="Cargando revisiones" className="revisiones-cargando">
          {[0, 1].map((i) => (
            <Esqueleto key={i} alto="150px" radio="16px" />
          ))}
        </div>
      )}

      {datos && datos.length === 0 && (
        <section className="tarjeta-panel">
          <EstadoVacio icono={iconoVacio} titulo={tituloVacio}>
            {textoVacio}
          </EstadoVacio>
        </section>
      )}

      {datos && datos.length > 0 && (
        <div className="revisiones-lista">
          {datos.map((revision) => (
            <TarjetaRevision key={revision.id} revision={revision} onActualizado={recargar} />
          ))}
        </div>
      )}
    </>
  )
}
