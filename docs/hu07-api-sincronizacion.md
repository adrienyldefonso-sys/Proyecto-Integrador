# HU-07: Contrato de la API de sincronización (Spring Boot)

Base: http://127.0.0.1:8080 (emulador Android: http://10.0.2.2:8080)

## Reglas generales
- Todas las rutas /api/** exigen la cabecera `X-Api-Key`. Clave de desarrollo: `dev-demo-key`.
  Es provisional y se reemplazará por un inicio de sesión del agricultor.
- Los ids son UUID generados en el celular (el mismo id de Room). Enviar dos veces el mismo id
  es seguro: el servidor devuelve 200 con el registro que ya tenía.
- Las fechas van en ISO-8601 UTC, ej. `2026-11-21T18:00:00Z`.
  Room guarda milisegundos: `Instant.ofEpochMilli(valor).toString()`.
- `usuarioId` es el id del usuario en el servidor, no `usuario_local`.
  Para la demo se usa el de `agricultor1` (verlo en phpMyAdmin, tabla monitoreo_usuario).

## POST /api/parcelas (JSON)

    {
      "id": "3881c177-5378-4cba-bd47-04456c296e61",
      "usuarioId": "8685a586-20d9-4abb-90b8-dce6ec0bb8d5",
      "nombre": "Finca Nueva",
      "cultivo": "Papa",
      "referencia": "Chosica",
      "latitud": -12.05,
      "longitud": -77.03,
      "fechaRegistro": "2026-11-21T18:00:00Z"
    }

Respuestas: 201 creada, 200 ya existía, 400 datos inválidos (con `errores` por campo),
401 clave incorrecta, 409 conflicto.

## POST /api/diagnosticos (multipart/form-data) — disponible en la T3
- Parte `datos` (application/json): id, usuarioId, parcelaId (o null), plagaDetectada,
  confianza (0 a 100), nivelSeveridad, recomendacion, fechaCaptura.
- Parte `foto` (image/jpeg o image/png, máximo 10 MB).
- La parcela debe haberse sincronizado antes.

## Orden de envío desde el celular
1. Parcelas con estado PENDIENTE. 2. Diagnósticos con estado PENDIENTE.
3. Al recibir 200 o 201, marcar el registro como SINCRONIZADO.
