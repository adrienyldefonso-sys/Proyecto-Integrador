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