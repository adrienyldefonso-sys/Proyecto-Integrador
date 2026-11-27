import { describe, expect, it } from 'vitest'
import { construirRuta, parsearRuta } from './ruta'

describe('parsearRuta', () => {
  it('sin hash abre el resumen general', () => {
    expect(parsearRuta('')).toEqual({ vista: 'resumen', parcelaId: null })
    expect(parsearRuta('#/resumen')).toEqual({ vista: 'resumen', parcelaId: null })
  })

  it('reconoce las alertas y ruta', () => {
    expect(parsearRuta('#/alertas')).toEqual({ vista: 'alertas', parcelaId: null })
  })

  it('reconoce la lista de parcelas y una parcela concreta', () => {
    expect(parsearRuta('#/parcelas')).toEqual({ vista: 'parcelas', parcelaId: null })
    expect(parsearRuta('#/parcelas/abc-123')).toEqual({ vista: 'parcelas', parcelaId: 'abc-123' })
  })

  it('reconoce las revisiones', () => {
    expect(parsearRuta('#/revisiones')).toEqual({ vista: 'revisiones', parcelaId: null })
  })

  it('decodifica el id y no falla con un id mal codificado', () => {
    expect(parsearRuta('#/parcelas/a%20b').parcelaId).toBe('a b')
    expect(parsearRuta('#/parcelas/%E0%A4%A')).toEqual({ vista: 'parcelas', parcelaId: null })
  })

  it('una ruta desconocida vuelve al resumen', () => {
    expect(parsearRuta('#/otra')).toEqual({ vista: 'resumen', parcelaId: null })
  })
})

describe('construirRuta', () => {
  it('arma cada ruta', () => {
    expect(construirRuta('alertas')).toBe('#/alertas')
    expect(construirRuta('parcelas')).toBe('#/parcelas')
    expect(construirRuta('parcelas', 'abc 1')).toBe('#/parcelas/abc%201')
    expect(construirRuta('revisiones')).toBe('#/revisiones')
    expect(construirRuta('resumen')).toBe('#/resumen')
  })

  it('ida y vuelta conserva el id', () => {
    expect(parsearRuta(construirRuta('parcelas', 'x/y')).parcelaId).toBe('x/y')
  })
})

describe('usuarios', () => {
  it('reconoce la lista y una cuenta concreta', () => {
    expect(parsearRuta('#/usuarios')).toEqual({ vista: 'usuarios', parcelaId: null, usuarioId: null })
    expect(parsearRuta('#/usuarios/abc-123').usuarioId).toBe('abc-123')
  })

  it('arma la ruta de la lista y de una cuenta', () => {
    expect(construirRuta('usuarios')).toBe('#/usuarios')
    expect(construirRuta('usuarios', 'abc-123')).toBe('#/usuarios/abc-123')
  })
})
