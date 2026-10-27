package com.yldefonso.ojodchakra.ml

import com.yldefonso.ojodchakra.data.local.CatalogoRecomendaciones
import com.yldefonso.ojodchakra.data.model.Recomendacion

object MotorRecomendaciones {

    fun obtener(plagaClave: String): Recomendacion =
        CatalogoRecomendaciones.porClave[plagaClave.trim()]
            ?: CatalogoRecomendaciones.sinDatos
}