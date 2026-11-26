package com.yldefonso.ojodchakra.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Preferencias del usuario guardadas en el celular. El tema se observa desde Compose. */
object PreferenciasApp {

    const val TEMA_SISTEMA = 0
    const val TEMA_CLARO = 1
    const val TEMA_OSCURO = 2

    private const val ARCHIVO = "ojodchakra_ajustes"
    private const val CLAVE_TEMA = "tema"

    var tema by mutableStateOf(TEMA_SISTEMA)
        private set

    fun cargar(context: Context) {
        tema = context
            .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getInt(CLAVE_TEMA, TEMA_SISTEMA)
    }

    fun guardarTema(context: Context, valor: Int) {
        tema = valor
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit()
            .putInt(CLAVE_TEMA, valor)
            .apply()
    }
}
