import { useMemo } from 'react'
import { obtenerAlertas, obtenerParcelas } from '../api'
import { nivelesPorParcela } from '../formato'
import { useCarga } from '../hooks/useCarga'
import EncabezadoPagina from './EncabezadoPagina'
import ErrorCarga from './ErrorCarga'
import EstadoVacio from './EstadoVacio'
import Historial from './Historial'
import Icono from './Icono'
import ListaParcelas from './ListaParcelas'

export default function VistaParcelas({ parcelaId, onSeleccionar, onVolver }) {
  const parcelas = useCarga(obtenerParcelas, [])
  // Solo sirve para marcar la urgencia de cada parcela; si falla, la lista se muestra igual
  const alertas = useCarga(obtenerAlertas, [], { refrescoMs: 60000 })
  const niveles = useMemo(() => nivelesPorParcela(alertas.datos), [alertas.datos])

  const cabecera = (
    <EncabezadoPagina
      titulo="Parcelas"
      subtitulo="Consulta el historial de diagnósticos con su evidencia fotográfica."
      actualizado={parcelas.actualizado}
      onActualizar={parcelas.recargar}
    />
  )

  if (parcelas.error) {
    return (
      <>
        {cabecera}
        <ErrorCarga
          error={parcelas.error}
          titulo="No se pudieron cargar las parcelas"
          onReintentar={parcelas.recargar}
        />
      </>
    )
  }

  return (
    <>
      {cabecera}
      <div className={`maestro-detalle${parcelaId ? ' con-seleccion' : ''}`}>
        <ListaParcelas
          parcelas={parcelas.datos}
          cargando={parcelas.cargando}
          seleccionId={parcelaId}
          niveles={niveles}
          onSeleccionar={onSeleccionar}
        />
        <div className="detalle">
          {parcelaId ? (
            <>
              <button type="button" className="boton secundario solo-movil" onClick={onVolver}>
                <Icono nombre="atras" tamano={16} />
                Volver a las parcelas
              </button>
              <Historial key={parcelaId} parcelaId={parcelaId} />
            </>
          ) : (
            <section className="tarjeta-panel">
              <EstadoVacio icono="parcela" titulo="Elige una parcela">
                Selecciónala en la lista para ver su historial de diagnósticos.
              </EstadoVacio>
            </section>
          )}
        </div>
      </div>
    </>
  )
}
