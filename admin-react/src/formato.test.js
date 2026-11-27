import { describe, expect, it } from 'vitest'
import {
  distribucionPlagas,
  estadoRevision,
  formatearConfianza,
  formatearFecha,
  nivelSeveridad,
  nivelesPorParcela,
  nombrePlaga,
  porcentaje,
  rangoRapido,
  rutaDeFoto,
  severidadDePlaga,
  textoConteo,
  textoRelativo,
  tonoDe,
} from './formato'

describe('nombrePlaga', () => {
  it('traduce las etiquetas del modelo', () => {
    expect(nombrePlaga('Potato___Late_blight')).toBe('Tizón tardío')
    expect(nombrePlaga('Potato___healthy')).toBe('Planta sana')
  })

  it('deja igual una etiqueta desconocida', () => {
    expect(nombrePlaga('Otra_plaga')).toBe('Otra_plaga')
  })
})

describe('nivelSeveridad', () => {
  it('reconoce los niveles conocidos', () => {
    expect(nivelSeveridad('ALTA').texto).toBe('Severidad alta')
    expect(nivelSeveridad('NINGUNA').texto).toBe('Sin riesgo')
  })

  it('un valor vacío o desconocido queda sin evaluar', () => {
    expect(nivelSeveridad('').clave).toBe('SIN')
    expect(nivelSeveridad(undefined).texto).toBe('Sin evaluar')
  })
})

describe('tonoDe y severidadDePlaga', () => {
  it('asigna un tono a cada severidad', () => {
    expect(tonoDe('ALTA')).toBe('alta')
    expect(tonoDe('MEDIA')).toBe('media')
    expect(tonoDe('NINGUNA')).toBe('ok')
    expect(tonoDe('SIN')).toBe('neutro')
    expect(tonoDe('inventada')).toBe('neutro')
  })

  it('deduce la severidad de la plaga', () => {
    expect(severidadDePlaga('Potato___Late_blight')).toBe('ALTA')
    expect(severidadDePlaga('Potato___Early_blight')).toBe('MEDIA')
    expect(severidadDePlaga('Potato___healthy')).toBe('NINGUNA')
    expect(severidadDePlaga('Otra')).toBe('SIN')
  })
})

describe('estadoRevision', () => {
  it('describe cada estado de la revisión', () => {
    expect(estadoRevision('SOLICITADA').corto).toBe('Pendiente')
    expect(estadoRevision('CONFIRMADA').tono).toBe('ok')
    expect(estadoRevision('CORREGIDA').tono).toBe('terracota')
  })

  it('un diagnóstico sin revisión no tiene estado', () => {
    expect(estadoRevision('NINGUNA')).toBeNull()
    expect(estadoRevision(undefined)).toBeNull()
  })
})

describe('textoConteo', () => {
  it('usa singular y plural', () => {
    expect(textoConteo(0)).toBe('Sin diagnósticos')
    expect(textoConteo(1)).toBe('1 diagnóstico')
    expect(textoConteo(5)).toBe('5 diagnósticos')
  })
})

describe('formatearConfianza', () => {
  it('redondea a un decimal', () => {
    expect(formatearConfianza(97.456)).toBe('97.5 %')
    expect(formatearConfianza(98)).toBe('98.0 %')
  })
})

describe('formatearFecha', () => {
  it('una fecha inválida muestra una raya', () => {
    expect(formatearFecha('no-es-fecha')).toBe('—')
  })

  it('una fecha válida incluye el año', () => {
    expect(formatearFecha('2026-11-21T18:42:00Z')).toContain('2026')
  })
})

describe('rutaDeFoto', () => {
  it('convierte una URL absoluta en una ruta del proxy', () => {
    expect(rutaDeFoto('http://127.0.0.1:8000/media/diagnosticos/a.jpg')).toBe(
      '/media/diagnosticos/a.jpg',
    )
  })

  it('deja igual una ruta relativa', () => {
    expect(rutaDeFoto('/media/diagnosticos/a.jpg')).toBe('/media/diagnosticos/a.jpg')
  })

  it('agrega /media a una ruta que empieza en /diagnosticos', () => {
    expect(rutaDeFoto('/diagnosticos/a.jpg')).toBe('/media/diagnosticos/a.jpg')
  })

  it('sin foto o con una ruta ajena devuelve nulo', () => {
    expect(rutaDeFoto(null)).toBeNull()
    expect(rutaDeFoto('')).toBeNull()
    expect(rutaDeFoto('http://127.0.0.1:8000/otra/a.jpg')).toBeNull()
  })
})

describe('porcentaje', () => {
  it('redondea y no divide entre cero', () => {
    expect(porcentaje(1, 4)).toBe(25)
    expect(porcentaje(1, 3)).toBe(33)
    expect(porcentaje(0, 0)).toBe(0)
  })
})

describe('distribucionPlagas', () => {
  const reparto = distribucionPlagas({
    Potato___healthy: 1,
    Potato___Late_blight: 3,
    Potato___Early_blight: 1,
  })

  it('ordena de mayor a menor y desempata por nombre', () => {
    expect(reparto.map((d) => d.nombre)).toEqual(['Tizón tardío', 'Planta sana', 'Tizón temprano'])
  })

  it('calcula el porcentaje de cada plaga', () => {
    expect(reparto.map((d) => d.porcentaje)).toEqual([60, 20, 20])
  })

  it('asigna el tono según la severidad de la plaga', () => {
    expect(reparto.map((d) => d.tono)).toEqual(['alta', 'ok', 'media'])
  })

  it('una plaga desconocida queda en tono neutro y un reparto vacío no falla', () => {
    expect(distribucionPlagas({ Otra: 2 })[0].tono).toBe('neutro')
    expect(distribucionPlagas(undefined)).toEqual([])
  })
})

describe('textoRelativo', () => {
  const ahora = new Date('2026-11-27T12:00:00Z')
  const hace = (ms) => new Date(ahora.getTime() - ms).toISOString()

  it('muestra minutos, horas y días', () => {
    expect(textoRelativo(hace(20_000), ahora)).toBe('justo ahora')
    expect(textoRelativo(hace(5 * 60_000), ahora)).toBe('hace 5 min')
    expect(textoRelativo(hace(3 * 3_600_000), ahora)).toBe('hace 3 h')
    expect(textoRelativo(hace(86_400_000), ahora)).toBe('hace 1 día')
    expect(textoRelativo(hace(5 * 86_400_000), ahora)).toBe('hace 5 días')
  })

  it('pasa a semanas y a meses', () => {
    expect(textoRelativo(hace(21 * 86_400_000), ahora)).toBe('hace 3 semanas')
    expect(textoRelativo(hace(95 * 86_400_000), ahora)).toBe('hace 3 meses')
  })

  it('una fecha inválida muestra una raya y una futura, justo ahora', () => {
    expect(textoRelativo('nada', ahora)).toBe('—')
    expect(textoRelativo(new Date(ahora.getTime() + 60_000).toISOString(), ahora)).toBe('justo ahora')
  })
})

describe('rangoRapido', () => {
  it('cuenta hoy como el último día del rango', () => {
    const r = rangoRapido(7, new Date('2026-11-27T15:00:00Z'))
    expect(r).toEqual({ desde: '2026-11-21', hasta: '2026-11-27' })
  })

  it('usa la fecha de Lima y no la de UTC', () => {
    const r = rangoRapido(1, new Date('2026-11-28T03:00:00Z'))
    expect(r).toEqual({ desde: '2026-11-27', hasta: '2026-11-27' })
  })
})

describe('nivelesPorParcela', () => {
  it('relaciona cada parcela con su nivel de urgencia', () => {
    const alertas = {
      grupos: {
        ALTA: [{ parcela: { id: 'a' } }],
        MEDIA: [],
        SIN_ALERTA: [{ parcela: { id: 'b' } }, { parcela: { id: 'c' } }],
      },
    }
    expect(nivelesPorParcela(alertas)).toEqual({ a: 'ALTA', b: 'SIN_ALERTA', c: 'SIN_ALERTA' })
  })

  it('sin datos devuelve un mapa vacío', () => {
    expect(nivelesPorParcela(null)).toEqual({})
  })
})
