import { useState } from 'react'
import { cambiarEstadoUsuario, cerrarSesionesUsuario, obtenerUsuario } from '../api'
import { formatearFecha, nombrePlaga, textoRelativo } from '../formato'
import { useCarga } from '../hooks/useCarga'
import { inicialesDe } from '../usuarios'
import EstadoVacio from './EstadoVacio'
import ErrorCarga from './ErrorCarga'
import Esqueleto from './Esqueleto'
import Insignia from './Insignia'

// Textos de la confirmación antes de cambiar algo que el agricultor siente de inmediato
const CONFIRMACIONES = {
  desactivar: {
    titulo: '¿Desactivar esta cuenta?',
    texto: 'Se cerrarán sus sesiones y no podrá entrar hasta que la actives de nuevo. Sus datos no se borran.',
    boton: 'Sí, desactivar',
  },
  cerrar: {
    titulo: '¿Cerrar todas sus sesiones?',
    texto: 'Tendrá que volver a iniciar sesión en cada celular. Sus datos no se borran.',
    boton: 'Sí, cerrar sesiones',
  },
}

export default function UsuarioDetalle({ usuarioId, onCambio }) {
  const detalle = useCarga(() => obtenerUsuario(usuarioId), [usuarioId])
  const [confirmando, setConfirmando] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [aviso, setAviso] = useState(null)

  if (detalle.error) {
    return (
      <ErrorCarga
        error={detalle.error}
        titulo="No se pudo cargar esta cuenta"
        onReintentar={detalle.recargar}
      />
    )
  }

  if (detalle.cargando || !detalle.datos) {
    return (
      <div className="parcelas-cargando" role="status" aria-label="Cargando usuario">
        <Esqueleto alto="120px" radio="18px" />
        <Esqueleto alto="220px" radio="18px" />
      </div>
    )
  }

  const u = detalle.datos
  const nombre = u.nombre || u.usuario

  const ejecutar = async (accion) => {
    setEnviando(true)
    setAviso(null)
    try {
      if (accion === 'cerrar') {
        const { cerradas } = await cerrarSesionesUsuario(u.id)
        setAviso({
          tipo: 'ok',
          texto: cerradas === 1 ? 'Se cerró 1 sesión.' : `Se cerraron ${cerradas} sesiones.`,
        })
      } else {
        const activar = accion === 'activar'
        await cambiarEstadoUsuario(u.id, activar)
        setAviso({
          tipo: 'ok',
          texto: activar
            ? 'Cuenta activada. Ya puede volver a entrar.'
            : 'Cuenta desactivada. Sus sesiones se cerraron.',
        })
      }
      setConfirmando(null)
      detalle.recargar()
      onCambio()
    } catch (error) {
      setAviso({ tipo: 'error', texto: error.message })
    } finally {
      setEnviando(false)
    }
  }

  const confirmacion = confirmando ? CONFIRMACIONES[confirmando] : null

  return (
    <article className="usuario-detalle" aria-label={`Ficha de ${nombre}`}>
      <header className="usuario-cabecera">
        <span className="avatar avatar-grande" aria-hidden="true">
          {inicialesDe(u.nombre, u.usuario)}
        </span>
        <div className="usuario-cabecera-texto">
          <h2 className="usuario-nombre">{nombre}</h2>
          <p className="muted">
            @{u.usuario}
            {u.telefono ? ` · Celular ${u.telefono}` : ' · Sin celular'}
          </p>
          <p className="muted">Registrado el {formatearFecha(u.fecha_registro)}</p>
        </div>
        <Insignia tono={u.activo ? 'ok' : 'neutro'} texto={u.activo ? 'Cuenta activa' : 'Cuenta desactivada'} />
      </header>

      <div className="stats">
        <div className="stat">
          <span className="stat-valor">{u.parcelas}</span>
          <span className="stat-etiqueta">Parcelas</span>
        </div>
        <div className="stat">
          <span className="stat-valor">{u.reportes}</span>
          <span className="stat-etiqueta">Reportes</span>
        </div>
        <div className="stat">
          <span className="stat-valor">{u.sesiones_vigentes}</span>
          <span className="stat-etiqueta">Sesiones abiertas</span>
        </div>
      </div>

      <section className="usuario-seccion" aria-label="Acciones de la cuenta">
        <h3 className="titulo-seccion">Acciones</h3>
        <div className="acciones">
          {u.activo ? (
            <button
              type="button"
              className="boton secundario peligro"
              disabled={enviando}
              onClick={() => setConfirmando('desactivar')}
            >
              Desactivar cuenta
            </button>
          ) : (
            <button type="button" className="boton" disabled={enviando} onClick={() => ejecutar('activar')}>
              Activar cuenta
            </button>
          )}
          <button
            type="button"
            className="boton secundario"
            disabled={enviando || u.sesiones_vigentes === 0}
            onClick={() => setConfirmando('cerrar')}
          >
            Cerrar todas las sesiones
          </button>
        </div>

        {confirmacion && (
          <div className="confirmacion" role="alertdialog" aria-label={confirmacion.titulo}>
            <h4>{confirmacion.titulo}</h4>
            <p>{confirmacion.texto}</p>
            <div className="acciones">
              <button type="button" className="boton secundario" disabled={enviando} onClick={() => setConfirmando(null)}>
                Cancelar
              </button>
              <button type="button" className="boton peligro-fuerte" disabled={enviando} onClick={() => ejecutar(confirmando)}>
                {enviando ? 'Guardando...' : confirmacion.boton}
              </button>
            </div>
          </div>
        )}

        {aviso && (
          <p className={`aviso-usuario ${aviso.tipo}`} role="status">
            {aviso.texto}
          </p>
        )}
      </section>

      <section className="usuario-seccion">
        <h3 className="titulo-seccion">Inicios de sesión</h3>
        {u.inicios.length === 0 ? (
          <EstadoVacio icono="reloj" titulo="Aún no ha iniciado sesión">
            Cuando entre desde la app, su inicio aparecerá aquí.
          </EstadoVacio>
        ) : (
          <ul className="lista-simple">
            {u.inicios.map((inicio, posicion) => (
              <li key={`${inicio.fecha}-${posicion}`}>
                <span>{formatearFecha(inicio.fecha)}</span>
                <span className="muted">{textoRelativo(inicio.fecha)}</span>
              </li>
            ))}
          </ul>
        )}
        {u.inicios.length >= 30 && <p className="muted nota">Se muestran los últimos 30 inicios.</p>}
      </section>

      <section className="usuario-seccion">
        <h3 className="titulo-seccion">Parcelas ({u.lista_parcelas.length})</h3>
        {u.lista_parcelas.length === 0 ? (
          <p className="muted">Todavía no tiene parcelas registradas.</p>
        ) : (
          <ul className="lista-simple">
            {u.lista_parcelas.map((parcela) => (
              <li key={parcela.id}>
                <span className="parcela-nombre">{parcela.nombre}</span>
                <span className="muted">
                  {parcela.cultivo || 'Sin cultivo'} · creada {formatearFecha(parcela.fecha_creacion)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="usuario-seccion">
        <h3 className="titulo-seccion">Últimos reportes</h3>
        {u.ultimos_reportes.length === 0 ? (
          <p className="muted">Todavía no tiene reportes.</p>
        ) : (
          <ul className="lista-simple">
            {u.ultimos_reportes.map((reporte) => (
              <li key={reporte.id}>
                <span>{nombrePlaga(reporte.plaga_detectada)}</span>
                <span className="muted">
                  {reporte.parcela || 'Sin parcela'} · {formatearFecha(reporte.fecha_captura)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </article>
  )
}
