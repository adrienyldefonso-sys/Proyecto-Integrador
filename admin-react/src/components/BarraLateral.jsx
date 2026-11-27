import { useState } from 'react'
import { construirRuta } from '../ruta'
import Icono from './Icono'
import Marca from './Marca'

const SECCIONES = [
  { vista: 'resumen', texto: 'Resumen general', icono: 'resumen' },
  { vista: 'alertas', texto: 'Alertas y ruta', icono: 'alerta' },
  { vista: 'parcelas', texto: 'Parcelas', icono: 'parcela' },
  { vista: 'revisiones', texto: 'Revisiones', icono: 'revisar' },
  { vista: 'usuarios', texto: 'Usuarios', icono: 'persona' },
]

function Sesion({ sesion }) {
  if (!sesion) return null

  if (!sesion.autenticado) {
    return (
      <div className="sesion">
        <div className="sesion-datos">
          <p className="sesion-nombre">Sin sesión iniciada</p>
          <p className="sesion-rol">Entra con tu cuenta de agrónomo</p>
        </div>
        <a className="boton pequeno" href="/admin/login/?next=/admin/" target="_blank" rel="noreferrer">
          Entrar
        </a>
      </div>
    )
  }

  return (
    <div className="sesion">
      <span className="avatar" aria-hidden="true">
        {sesion.usuario.charAt(0).toUpperCase()}
      </span>
      <div className="sesion-datos">
        <p className="sesion-nombre">{sesion.usuario}</p>
        <p className="sesion-rol">{sesion.es_agronomo ? 'Agrónomo' : 'Sin permisos de agrónomo'}</p>
      </div>
      <a className="sesion-salir" href="/admin/logout/" title="Cerrar sesión" aria-label="Cerrar sesión">
        <Icono nombre="salir" tamano={18} />
      </a>
    </div>
  )
}

const CLAVE_TEMA = 'ojodchakra-tema'

// Cambia entre modo claro y oscuro, y recuerda la elección en este navegador
function TemaToggle() {
  const [oscuro, setOscuro] = useState(() => document.documentElement.dataset.tema === 'oscuro')

  const alternar = () => {
    const siguiente = !oscuro
    setOscuro(siguiente)
    document.documentElement.dataset.tema = siguiente ? 'oscuro' : 'claro'
    try {
      localStorage.setItem(CLAVE_TEMA, siguiente ? 'oscuro' : 'claro')
    } catch {
      // Sin almacenamiento, el cambio dura hasta recargar
    }
  }

  return (
    <button type="button" className="tema-boton" onClick={alternar} aria-pressed={oscuro}>
      {oscuro ? 'Usar modo claro' : 'Usar modo oscuro'}
    </button>
  )
}

export default function BarraLateral({ vista, pendientes, sesion }) {
  return (
    <aside className="barra-lateral">
      <div className="marca-lateral">
        <Marca />
      </div>

      <nav className="nav" aria-label="Secciones del panel">
        {SECCIONES.map((seccion) => {
          const activa = seccion.vista === vista
          return (
            <a
              key={seccion.vista}
              href={construirRuta(seccion.vista)}
              className={`nav-enlace${activa ? ' activa' : ''}`}
              aria-current={activa ? 'page' : undefined}
            >
              <Icono nombre={seccion.icono} tamano={20} />
              <span>{seccion.texto}</span>
              {seccion.vista === 'revisiones' && pendientes > 0 && (
                <span className="nav-contador" aria-label={`${pendientes} pendientes`}>
                  {pendientes}
                </span>
              )}
            </a>
          )
        })}
      </nav>

      <div className="nav-pie">
        <a className="nav-secundario" href="/admin/" target="_blank" rel="noreferrer">
          <Icono nombre="externo" tamano={16} />
          Administración de Django
        </a>
        <TemaToggle />
        <Sesion sesion={sesion} />
      </div>
    </aside>
  )
}
