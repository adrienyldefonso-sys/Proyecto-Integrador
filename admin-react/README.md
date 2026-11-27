# Panel del agrónomo (OjoDChakra)

Frontend web en React (Vite). Consume la API de Django (`admin-django/`).

## Cómo ejecutarlo

1. En `admin-django/`: `python manage.py runserver`
2. En `admin-react/`: `npm install` (solo la primera vez) y `npm run dev`
3. Abrir http://localhost:5173

Vite reenvía `/api`, `/admin`, `/media` y `/static` a Django (puerto 8000),
por eso el navegador usa un solo origen y la sesión funciona sin configuración extra.

## Pantallas

| Ruta | Qué muestra |
|---|---|
| `#/alertas` | Resumen por urgencia, ruta de visitas numerada y cómo se calcula la urgencia |
| `#/parcelas` y `#/parcelas/<id>` | Lista de parcelas y su historial con fotos, filtros por fecha y estado de revisión |
| `#/revisiones` | Diagnósticos que el agricultor pidió revisar: confirmar o corregir |

Las rutas usan el hash de la URL, así que se pueden enlazar y funciona el botón "atrás".

## Diseño

Mismos colores, bordes y formas que la app móvil (estilo AgriTech). Los colores y medidas
están como variables al inicio de `src/index.css`. En pantallas pequeñas el menú pasa a una
barra inferior y la lista de parcelas y su historial se muestran de una en una.

## Pruebas

`npm test` (Vitest) prueba los formatos, la lógica de las rutas y el resumen de datos.

## Inicio de sesión (provisional)

Se entra por el login de Django: http://localhost:5173/admin/login/?next=/admin/
con una cuenta de agrónomo (datos de desarrollo: `agronomo` / `agronomo123`,
creados con `cargar_datos_demo`).
