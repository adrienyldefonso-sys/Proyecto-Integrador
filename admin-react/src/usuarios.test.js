import { describe, expect, it } from 'vitest'
import { filtrarCuentas, inicialesDe } from './usuarios'

describe('inicialesDe', () => {
  it('usa la primera letra del nombre y del apellido', () => {
    expect(inicialesDe('Juan Pérez', 'juan')).toBe('JP')
  })

  it('con un solo nombre usa las dos primeras letras', () => {
    expect(inicialesDe('Juan', 'juan')).toBe('JU')
  })

  it('sin nombre usa el usuario', () => {
    expect(inicialesDe('', 'maria.r')).toBe('MA')
  })
})

describe('filtrarCuentas', () => {
  const cuentas = [
    { id: '1', nombre: 'Juan Pérez', usuario: 'juan', telefono: '912345678', activo: true },
    { id: '2', nombre: 'María', usuario: 'maria', telefono: '987654321', activo: false },
  ]

  it('filtra por estado de la cuenta', () => {
    expect(filtrarCuentas(cuentas, { filtro: 'activas' }).map((c) => c.id)).toEqual(['1'])
    expect(filtrarCuentas(cuentas, { filtro: 'desactivadas' }).map((c) => c.id)).toEqual(['2'])
  })

  it('busca por nombre, usuario o celular sin distinguir mayúsculas', () => {
    expect(filtrarCuentas(cuentas, { texto: '9876' }).map((c) => c.id)).toEqual(['2'])
    expect(filtrarCuentas(cuentas, { texto: 'JUAN' }).map((c) => c.id)).toEqual(['1'])
  })
})
