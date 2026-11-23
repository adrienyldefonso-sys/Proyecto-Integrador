import { describe, expect, it } from 'vitest'
import {
  formatearConfianza,
  formatearFecha,
  nivelSeveridad,
  nombrePlaga,
  rutaDeFoto,
  textoConteo,
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

  it('sin foto devuelve nulo', () => {
    expect(rutaDeFoto(null)).toBeNull()
    expect(rutaDeFoto('')).toBeNull()
  })
})