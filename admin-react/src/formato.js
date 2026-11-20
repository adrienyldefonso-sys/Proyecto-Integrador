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

export const nombrePlaga = (clave) => NOMBRES_PLAGA[clave] ?? clave

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

export const formatearConfianza = (valor) => `${Number(valor).toFixed(1)} %`