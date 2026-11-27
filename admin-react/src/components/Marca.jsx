import Logo from './Logo'

export default function Marca({ tamano = 40 }) {
  return (
    <span className="marca">
      <span className="marca-logo">
        <Logo tamano={tamano} />
      </span>
      <span className="marca-texto">
        <span className="marca-nombre">
          <span>OJO</span>
          <span className="marca-d">D</span>
          <span>CHAKRA</span>
        </span>
        <span className="marca-lema">Panel del agrónomo</span>
      </span>
    </span>
  )
}
