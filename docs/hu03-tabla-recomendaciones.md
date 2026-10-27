# HU-03: Diseño de la tabla de recomendaciones

Relación plaga → tratamiento sugerido. La clave de búsqueda es la etiqueta
exacta de `labels.txt`, que es lo que devuelve el modelo de IA.

| Campo | Tipo | Descripción |
|---|---|---|
| plagaClave | String | Etiqueta del modelo, ej. Potato___Late_blight |
| nombre | String | Nombre en español para mostrar |
| descripcion | String | Qué es, en una frase |
| nivelSeveridad | String | NINGUNA, BAJA, MEDIA o ALTA |
| tratamiento | String | Qué hacer ahora |
| prevencion | String | Cómo evitarlo después |

| plagaClave | nombre | nivelSeveridad |
|---|---|---|
| Potato___Early_blight | Tizón temprano | MEDIA |
| Potato___Late_blight | Tizón tardío | ALTA |
| Potato___healthy | Planta sana | NINGUNA |