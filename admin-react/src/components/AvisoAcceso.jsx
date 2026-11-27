import Icono from './Icono'
import Logo from './Logo'

export default function AvisoAcceso({ mensaje, onReintentar }) {
  return (
    <section className="acceso" role="alert">
      <Logo tamano={56} />
      <h2>Acceso restringido</h2>
      <p>{mensaje}</p>
      <ol className="pasos">
        <li>Inicia sesión con una cuenta de agrónomo (se abre en una pestaña nueva).</li>
        <li>
          Vuelve a esta pestaña y pulsa <strong>Reintentar</strong>.
        </li>
      </ol>
      <div className="acciones">
        <a className="boton" href="/admin/login/?next=/admin/" target="_blank" rel="noreferrer">
          Iniciar sesión
          <Icono nombre="externo" tamano={16} />
        </a>
        <button type="button" className="boton secundario" onClick={onReintentar}>
          <Icono nombre="actualizar" tamano={16} />
          Reintentar
        </button>
      </div>
      <p className="nota">Acceso provisional de desarrollo con el inicio de sesión de Django.</p>
    </section>
  )
}
