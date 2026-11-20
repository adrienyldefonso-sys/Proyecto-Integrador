export default function AvisoAcceso({ mensaje, onReintentar }) {
  return (
    <section className="aviso">
      <h3>Acceso restringido</h3>
      <p>{mensaje}</p>
      <p className="muted">
        Inicio de sesión provisional de desarrollo: entra con una cuenta de agrónomo
        en la pestaña nueva, vuelve aquí y pulsa Reintentar.
      </p>
      <div className="acciones">
        <a className="boton" href="/admin/login/?next=/admin/" target="_blank" rel="noreferrer">
          Iniciar sesión
        </a>
        <button type="button" className="boton secundario" onClick={onReintentar}>
          Reintentar
        </button>
      </div>
    </section>
  )
}