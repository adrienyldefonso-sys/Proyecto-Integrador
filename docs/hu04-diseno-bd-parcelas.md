# HU-04: Diseño de la base de datos de parcelas

Tabla `parcelas`: cada parcela pertenece a un agricultor y puede tener
muchos diagnósticos (relación 1 a N con `diagnosticos.parcela_id`).

| Campo | Tipo Room | Equivalente MySQL | Descripción |
|---|---|---|---|
| id | String (UUID), clave primaria | CHAR(36) | Identificador único |
| usuario_id | String | CHAR(36) | Agricultor dueño de la parcela |
| nombre | String | VARCHAR(100) | Nombre de la parcela |
| cultivo | String | VARCHAR(50) | Ej. Papa |
| referencia | String | VARCHAR(150) | Texto descriptivo del lugar, ej. "Sector Pampa Alta" (nuevo en versión 2) |
| latitud | Double | DECIMAL(10,7) | Ubicación obtenida por GPS |
| longitud | Double | DECIMAL(10,7) | Ubicación obtenida por GPS |
| fecha_registro | Long | DATETIME | Fecha de creación en milisegundos |
| estado_sincronizacion | String | VARCHAR(20) | PENDIENTE o SINCRONIZADO |

## Migración de Room 1 a 2

Se agrega la columna `referencia` con valor por defecto vacío, de modo que
las parcelas ya guardadas no se pierden:

    ALTER TABLE parcelas ADD COLUMN referencia TEXT NOT NULL DEFAULT ''

## Consulta de la lista

La pantalla de parcelas muestra cuántos diagnósticos tiene cada una,
calculado con un conteo sobre `diagnosticos.parcela_id`.
