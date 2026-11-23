package com.yldefonso.ojodchakra.red

object ConfigApi {
    // 10.0.2.2 es la laptop vista desde el emulador. Con un celular físico va la IP de la laptop en el Wi-Fi.
    const val URL_BASE = "http://10.0.2.2:8080"

    // Provisionales: hasta que exista el inicio de sesión del agricultor.
    const val CLAVE_API = "dev-demo-key"

    // El id de agricultor1 en TU base (phpMyAdmin → monitoreo_usuario). Cambia en cada laptop.
    const val USUARIO_SERVIDOR_ID = "PEGA-AQUI-EL-ID-DE-AGRICULTOR1"
}