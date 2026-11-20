# Panel del agrónomo (OjoDChakra)

Frontend web en React (Vite). Consume la API de Django (`admin-django/`).

## Cómo ejecutarlo

1. En `admin-django/`: `python manage.py runserver`
2. En `admin-react/`: `npm install` (solo la primera vez) y `npm run dev`
3. Abrir http://localhost:5173

Vite reenvía `/api`, `/admin`, `/media` y `/static` a Django (puerto 8000),
por eso el navegador usa un solo origen y la sesión funciona sin configuración extra.

## Inicio de sesión (provisional)

Hasta que exista el inicio de sesión propio (HU-07), se entra por el login de Django:
http://localhost:5173/admin/login/?next=/admin/ con una cuenta de agrónomo
(datos de desarrollo: `agronomo` / `agronomo123`, creados con `cargar_datos_demo`).