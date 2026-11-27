export default function Esqueleto({ ancho = '100%', alto = '1rem', radio = '8px', className = '' }) {
  return (
    <span
      className={`esqueleto ${className}`.trim()}
      style={{ width: ancho, height: alto, borderRadius: radio }}
      aria-hidden="true"
    />
  )
}
