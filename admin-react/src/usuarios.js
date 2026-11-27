// Iniciales para el avatar: "Juan Pérez" da "JP"; sin nombre se usa el usuario.
export function inicialesDe(nombre, usuario = '') {
  const partes = (nombre || '').trim().split(/\s+/).filter(Boolean)
  if (partes.length >= 2) return (partes[0][0] + partes[1][0]).toUpperCase()
  return (partes[0] || usuario || '?').slice(0, 2).toUpperCase()
}

export const FILTROS_CUENTAS = [
  ['todos', 'Todas'],
  ['activas', 'Activas'],
  ['desactivadas', 'Desactivadas'],
]

export function filtrarCuentas(cuentas, { texto = '', filtro = 'todos' } = {}) {
  const termino = texto.trim().toLowerCase()
  return cuentas.filter((cuenta) => {
    if (filtro === 'activas' && !cuenta.activo) return false
    if (filtro === 'desactivadas' && cuenta.activo) return false
    if (!termino) return true
    return `${cuenta.nombre} ${cuenta.usuario} ${cuenta.telefono}`.toLowerCase().includes(termino)
  })
}
