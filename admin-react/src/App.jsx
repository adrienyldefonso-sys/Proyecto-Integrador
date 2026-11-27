import { useEffect } from 'react'
import { obtenerRevisiones, obtenerSesion } from './api'
import BarraLateral from './components/BarraLateral'
import Marca from './components/Marca'
import Resumen from './components/Resumen'
import Revisiones from './components/Revisiones'
import Tablero from './components/Tablero'
import Usuarios from './components/Usuarios'
import VistaParcelas from './components/VistaParcelas'
import { useCarga } from './hooks/useCarga'
import { useRuta } from './hooks/useRuta'

const TITULOS = {
  resumen: 'Resumen general',
  alertas: 'Alertas y ruta',
  parcelas: 'Parcelas',
  revisiones: 'Revisiones',
  usuarios: 'Usuarios',
}

export default function App() {
  const { vista, parcelaId, usuarioId, ir } = useRuta()
  const sesion = useCarga(obtenerSesion, [])
  // Solo alimenta el contador del menú; si falla (por ejemplo sin sesión), no se muestra
  const pendientes = useCarga(() => obtenerRevisiones('pendientes'), [], { refrescoMs: 60000 })

  useEffect(() => {
    document.title = `${TITULOS[vista]} · OjoDChakra`
  }, [vista])

  return (
    <div className="shell">
      <BarraLateral
        vista={vista}
        pendientes={pendientes.datos ? pendientes.datos.length : null}
        sesion={sesion.datos}
      />
      <main className="contenido">
        <div className="marca-movil">
          <Marca tamano={36} />
        </div>
        {vista === 'resumen' && <Resumen onIr={ir} />}
        {vista === 'alertas' && <Tablero onVerHistorial={(id) => ir('parcelas', id)} />}
        {vista === 'parcelas' && (
          <VistaParcelas
            parcelaId={parcelaId}
            onSeleccionar={(id) => ir('parcelas', id)}
            onVolver={() => ir('parcelas')}
          />
        )}
        {vista === 'revisiones' && <Revisiones />}
        {vista === 'usuarios' && (
          <Usuarios
            usuarioId={usuarioId}
            onSeleccionar={(id) => ir('usuarios', id)}
            onVolver={() => ir('usuarios')}
          />
        )}
      </main>
    </div>
  )
}
