
const NOMBRES_PLAGA = {
  Potato___Early_blight: 'Tizón temprano',
  Potato___Late_blight: 'Tizón tardío',
  Potato___healthy: 'Planta sana',
}

const SEVERIDADES = {
  ALTA: { clave: 'ALTA', texto: 'Severidad alta' },
  MEDIA: { clave: 'MEDIA', texto: 'Severidad media' },
  BAJA: { clave: 'BAJA', texto: 'Severidad baja' },
  NINGUNA: { clave: 'NINGUNA', texto: 'Sin riesgo' },
}

export const nombrePlaga = (clave) =>
  NOMBRES_PLAGA[clave] ?? clave

export const nivelSeveridad = (valor) =>
  SEVERIDADES[valor] ?? { clave: 'SIN', texto: 'Sin evaluar' }

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

export const formatearConfianza = (valor) =>
  `${Number(valor).toFixed(1)} %`

// Construye la URL de una fotografía almacenada en Django.
export function rutaDeFoto(foto) {
  if (!foto) return ''

  const ruta = String(foto).trim()

  if (/^https?:\/\//i.test(ruta)) {
    return ruta
  }

  const base = (
    import.meta.env.VITE_MEDIA_URL ||
    'http://127.0.0.1:8000/media/'
  ).replace(/\/+$/, '')

  const archivo = ruta
    .replace(/^\/+/, '')
    .replace(/^media\//, '')

  return `${base}/${archivo}`
}
