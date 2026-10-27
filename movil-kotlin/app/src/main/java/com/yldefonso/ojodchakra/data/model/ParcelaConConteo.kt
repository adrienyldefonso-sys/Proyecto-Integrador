package com.yldefonso.ojodchakra.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ParcelaConConteo(
    @Embedded val parcela: Parcela,
    @ColumnInfo(name = "total_diagnosticos") val totalDiagnosticos: Int
)