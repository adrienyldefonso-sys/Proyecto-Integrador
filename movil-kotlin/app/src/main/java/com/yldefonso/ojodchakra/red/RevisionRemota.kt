package com.yldefonso.ojodchakra.red

/** Revisión que el agrónomo dejó en el servidor y que la app debe reflejar en el historial. */
data class RevisionRemota(
    val id: String,
    val estadoRevision: String,
    val etiquetaValidada: String?,
    val observacionAgronomo: String?,
    val fechaRevisionMillis: Long?
)
