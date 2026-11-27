export class ErrorApi extends Error {
  constructor(estado, mensaje) {
    super(mensaje)
    this.estado = estado
  }
}

export const esSinAcceso = (error) => error?.estado === 401 || error?.estado === 403

async function pedir(ruta) {
  let respuesta
  try {
    respuesta = await fetch(ruta, {
      credentials: 'same-origin',
      headers: { Accept: 'application/json' },
    })
  } catch {
    throw new ErrorApi(0, 'No se pudo conectar con el servidor. Revisa que Django esté corriendo.')
  }

  if (!respuesta.ok) {
    let mensaje = 'No se pudo completar la consulta.'
    try {
      const cuerpo = await respuesta.json()
      if (typeof cuerpo.detail === 'string') mensaje = cuerpo.detail
      else if (cuerpo.desde || cuerpo.hasta) mensaje = 'Revisa las fechas del filtro.'
    } catch {
      // La respuesta no era JSON: se deja el mensaje general
    }
    throw new ErrorApi(respuesta.status, mensaje)
  }
  return respuesta.json()
}

export const obtenerParcelas = () => pedir('/api/parcelas/')

export function obtenerHistorial(parcelaId, { desde = '', hasta = '' } = {}) {
  const parametros = new URLSearchParams()
  if (desde) parametros.set('desde', desde)
  if (hasta) parametros.set('hasta', hasta)
  const consulta = parametros.toString()
  return pedir(`/api/parcelas/${parcelaId}/historial/${consulta ? `?${consulta}` : ''}`)
}

export const obtenerAlertas = () => pedir('/api/alertas/')

export const obtenerSesion = () => pedir('/api/sesion/')

export const obtenerMetricas = () => pedir('/api/metricas/')

export const obtenerRevisiones = (estado = 'pendientes') =>
  pedir(`/api/revisiones/?estado=${encodeURIComponent(estado)}`)

function obtenerCookie(nombre) {
  const coincidencia = document.cookie.split('; ').find((c) => c.startsWith(`${nombre}=`))
  return coincidencia ? decodeURIComponent(coincidencia.split('=')[1]) : ''
}

function mensajeDeError(cuerpo) {
  if (typeof cuerpo.detail === 'string') return cuerpo.detail
  const primero = Object.values(cuerpo).flat()[0]
  return typeof primero === 'string' ? primero : 'No se pudo guardar el cambio.'
}

// Envía un cambio en JSON. Django exige el token CSRF en los cambios.
async function enviarJson(ruta, datos) {
  let respuesta
  try {
    respuesta = await fetch(ruta, {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        'X-CSRFToken': obtenerCookie('csrftoken'),
      },
      body: JSON.stringify(datos),
    })
  } catch {
    throw new ErrorApi(0, 'No se pudo conectar con el servidor. Revisa que Django esté corriendo.')
  }
  const cuerpo = await respuesta.json().catch(() => ({}))
  if (!respuesta.ok) throw new ErrorApi(respuesta.status, mensajeDeError(cuerpo))
  return cuerpo
}

// Envía la decisión del agrónomo sobre un diagnóstico.
export const decidirRevision = (diagnosticoId, datos) =>
  enviarJson(`/api/diagnosticos/${diagnosticoId}/revision/`, datos)

export const obtenerUsuarios = () => pedir('/api/usuarios/')

export const obtenerUsuario = (id) => pedir(`/api/usuarios/${encodeURIComponent(id)}/`)

export const cambiarEstadoUsuario = (id, activo) =>
  enviarJson(`/api/usuarios/${encodeURIComponent(id)}/estado/`, { activo })

export const cerrarSesionesUsuario = (id) =>
  enviarJson(`/api/usuarios/${encodeURIComponent(id)}/cerrar-sesiones/`, {})
