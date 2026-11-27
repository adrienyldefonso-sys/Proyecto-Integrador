// Rutas por hash (#/resumen, #/alertas, #/parcelas[/<id>], #/usuarios[/<id>], #/revisiones):
// permiten enlazar una parcela o una cuenta y usar el botón "atrás" del navegador.
function decodificar(texto) {
  try {
    return decodeURIComponent(texto)
  } catch {
    return null
  }
}

export function parsearRuta(hash) {
  const limpio = (hash || '').replace(/^#\/?/, '')
  const [seccion, id] = limpio.split('/')

  if (seccion === 'parcelas') {
    return { vista: 'parcelas', parcelaId: id ? decodificar(id) : null }
  }

  if (seccion === 'usuarios') {
    return { vista: 'usuarios', parcelaId: null, usuarioId: id ? decodificar(id) : null }
  }

  if (seccion === 'revisiones') return { vista: 'revisiones', parcelaId: null }
  if (seccion === 'alertas') return { vista: 'alertas', parcelaId: null }
  return { vista: 'resumen', parcelaId: null }
}

// `id` es el de la parcela o el de la cuenta, según la vista.
export function construirRuta(vista, id = null) {
  if (vista === 'parcelas') {
    return id ? `#/parcelas/${encodeURIComponent(id)}` : '#/parcelas'
  }
  if (vista === 'usuarios') {
    return id ? `#/usuarios/${encodeURIComponent(id)}` : '#/usuarios'
  }
  if (vista === 'revisiones') return '#/revisiones'
  if (vista === 'alertas') return '#/alertas'
  return '#/resumen'
}
