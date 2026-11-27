const NOMBRES_PLAGA = {
  Potato___Early_blight: 'Tizón temprano',
  Potato___Late_blight: 'Tizón tardío',
  Potato___healthy: 'Planta sana',
}

// Severidad que implica cada etiqueta del modelo (misma tabla que el catálogo de la app)
const SEVERIDAD_DE_PLAGA = {
  Potato___Late_blight: 'ALTA',
  Potato___Early_blight: 'MEDIA',
  Potato___healthy: 'NINGUNA',
}

const SEVERIDADES = {
  ALTA: { clave: 'ALTA', texto: 'Severidad alta' },
  MEDIA: { clave: 'MEDIA', texto: 'Severidad media' },
  BAJA: { clave: 'BAJA', texto: 'Severidad baja' },
  NINGUNA: { clave: 'NINGUNA', texto: 'Sin riesgo' },
}

const TONOS = { ALTA: 'alta', MEDIA: 'media', BAJA: 'ok', NINGUNA: 'ok', SIN: 'neutro' }

export const URGENCIAS = {
  ALTA: { clave: 'ALTA', texto: 'Urgencia alta', tono: 'alta' },
  MEDIA: { clave: 'MEDIA', texto: 'Urgencia media', tono: 'media' },
  SIN_ALERTA: { clave: 'SIN_ALERTA', texto: 'Sin alerta', tono: 'ok' },
}

const ESTADOS_REVISION = {
  SOLICITADA: { corto: 'Pendiente', texto: 'Revisión solicitada', tono: 'media' },
  CONFIRMADA: { corto: 'Confirmada', texto: 'Confirmado por el agrónomo', tono: 'ok' },
  CORREGIDA: { corto: 'Corregida', texto: 'Corregido por el agrónomo', tono: 'terracota' },
}

// Opciones que el agrónomo puede elegir al corregir un diagnóstico
export const OPCIONES_PLAGA = [
  'Potato___Early_blight',
  'Potato___Late_blight',
  'Potato___healthy',
]

export const nombrePlaga = (clave) =>
  NOMBRES_PLAGA[clave] ?? clave

export const nivelSeveridad = (valor) =>
  SEVERIDADES[valor] ?? { clave: 'SIN', texto: 'Sin evaluar' }

export const tonoDe = (clave) => TONOS[clave] ?? 'neutro'

export const severidadDePlaga = (clave) => SEVERIDAD_DE_PLAGA[clave] ?? 'SIN'

export const estadoRevision = (estado) => ESTADOS_REVISION[estado] ?? null

export function textoConteo(total) {
  if (total === 0) return 'Sin diagnósticos'
  if (total === 1) return '1 diagnóstico'
  return `${total} diagnósticos`
}

export function formatearFecha(iso) {
  const fecha = new Date(iso)
  if (Number.isNaN(fecha.getTime())) return '—'

  return fecha.toLocaleString('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'America/Lima',
  })
}

export function formatearHora(fecha) {
  return fecha.toLocaleTimeString('es-PE', {
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'America/Lima',
  })
}

export const formatearConfianza = (valor) =>
  `${Number(valor).toFixed(1)} %`

export function textoRelativo(iso, ahora = new Date()) {
  const fecha = new Date(iso)
  if (Number.isNaN(fecha.getTime())) return '—'
  const minutos = Math.floor((ahora.getTime() - fecha.getTime()) / 60000)
  if (minutos < 1) return 'justo ahora'
  if (minutos < 60) return `hace ${minutos} min`
  const horas = Math.floor(minutos / 60)
  if (horas < 24) return `hace ${horas} h`
  const dias = Math.floor(horas / 24)
  if (dias < 14) return dias === 1 ? 'hace 1 día' : `hace ${dias} días`
  if (dias < 60) return `hace ${Math.floor(dias / 7)} semanas`
  return `hace ${Math.floor(dias / 30)} meses`
}

export const porcentaje = (parte, total) => (total > 0 ? Math.round((100 * parte) / total) : 0)

// Reparto de diagnósticos por plaga, de mayor a menor, con el tono de su severidad
export function distribucionPlagas(porPlaga) {
  const entradas = Object.entries(porPlaga ?? {})
  const total = entradas.reduce((suma, [, cantidad]) => suma + cantidad, 0)
  return entradas
    .map(([clave, cantidad]) => ({
      clave,
      nombre: nombrePlaga(clave),
      total: cantidad,
      porcentaje: porcentaje(cantidad, total),
      tono: tonoDe(severidadDePlaga(clave)),
    }))
    .sort((a, b) => b.total - a.total || a.nombre.localeCompare(b.nombre, 'es'))
}

// Mapa id de parcela -> nivel de urgencia, a partir de la respuesta de /api/alertas/
export function nivelesPorParcela(alertas) {
  const mapa = {}
  for (const [nivel, items] of Object.entries(alertas?.grupos ?? {})) {
    for (const item of items) mapa[item.parcela.id] = nivel
  }
  return mapa
}

// Normaliza la ruta relativa de una fotografía almacenada en Django.
export function rutaDeFoto(url) {
  if (!url || typeof url !== 'string') return null

  const valor = url.trim()
  if (!valor) return null

  try {
    const ruta = new URL(valor, 'http://base.local').pathname

    if (ruta.startsWith('/media/')) {
      return ruta
    }

    if (ruta.startsWith('/diagnosticos/')) {
      return `/media${ruta}`
    }

    return null
  } catch {
    return null
  }
}

const FORMATO_DIA = new Intl.DateTimeFormat('en-CA', {
  timeZone: 'America/Lima',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
})

// Últimos `dias` días, contando hoy, como fechas AAAA-MM-DD en hora de Lima
export function rangoRapido(dias, hoy = new Date()) {
  const inicio = new Date(hoy.getTime() - (dias - 1) * 86400000)
  return { desde: FORMATO_DIA.format(inicio), hasta: FORMATO_DIA.format(hoy) }
}
