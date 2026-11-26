package com.yldefonso.ojodchakra.ml

/** Decide si una foto se acepta como hoja de papa o se rechaza, antes de mostrar o guardar nada. */
object DecisionHoja {

    /** Etiqueta de la clase de rechazo del modelo v2 (fotos que no son hojas de papa). */
    const val ETIQUETA_RECHAZO = "No_identificado"

    /** Porcentaje mínimo para aceptar un diagnóstico. Provisional: se calibra con la evaluación del modelo. */
    const val UMBRAL_CONFIANZA = 70f

    const val CONSEJO =
        "Acércate a la hoja, con buena luz y sin otros objetos alrededor, e inténtalo de nuevo."

    enum class MotivoRechazo(val texto: String) {
        NO_ES_HOJA("No se detectó una hoja de papa en la foto."),
        CONFIANZA_BAJA("No se pudo identificar la hoja con seguridad.")
    }

    sealed interface Veredicto {
        data class Aceptada(val resultado: ClassificationResult) : Veredicto
        data class Rechazada(val motivo: MotivoRechazo) : Veredicto
    }

    fun evaluar(resultado: ClassificationResult, umbral: Float = UMBRAL_CONFIANZA): Veredicto = when {
        resultado.label == ETIQUETA_RECHAZO -> Veredicto.Rechazada(MotivoRechazo.NO_ES_HOJA)
        resultado.confidence < umbral -> Veredicto.Rechazada(MotivoRechazo.CONFIANZA_BAJA)
        else -> Veredicto.Aceptada(resultado)
    }

    fun mensaje(motivo: MotivoRechazo): String = "${motivo.texto} $CONSEJO"
}