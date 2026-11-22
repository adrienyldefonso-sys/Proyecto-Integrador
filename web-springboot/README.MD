
# API del agricultor (Spring Boot)

Recibe lo que envía la app móvil y lo guarda en la base MySQL compartida.

## Reglas
- Solo Django crea y modifica tablas (migraciones). Aquí `ddl-auto=none`.
- Versión: Spring Boot 3.5.16 (Hibernate 6.6), porque el MariaDB 10.4 de XAMPP no cumple
  el mínimo de Hibernate 7. Esta línea terminó su soporte gratuito el 30/06/2026.

## Cómo ejecutarlo
1. Encender MySQL en XAMPP.
2. `.\mvnw.cmd spring-boot:run`
3. Abrir http://127.0.0.1:8080/api/parcelas

## Pruebas
`.\mvnw.cmd test` (no necesitan base de datos)