package com.yldefonso.ojodchakra.data.local

import com.yldefonso.ojodchakra.data.model.Recomendacion

object CatalogoRecomendaciones {

    val recomendaciones: List<Recomendacion> = listOf(
        Recomendacion(
            plagaClave = "Potato___Early_blight",
            nombre = "Tizón temprano",
            descripcion = "Hongo que causa manchas marrones con anillos en las hojas más viejas.",
            nivelSeveridad = "MEDIA",
            tratamiento = "Retira y elimina las hojas muy afectadas. Aplica un fungicida autorizado para papa, siguiendo la etiqueta, y consulta a un ingeniero agrónomo. Evita regar sobre las hojas.",
            prevencion = "Rota los cultivos, elimina los restos de cosechas anteriores y mantén una fertilización equilibrada."
        ),
        Recomendacion(
            plagaClave = "Potato___Late_blight",
            nombre = "Tizón tardío",
            descripcion = "Enfermedad muy agresiva, favorecida por la humedad y el frío, que puede destruir el cultivo en pocos días.",
            nivelSeveridad = "ALTA",
            tratamiento = "Actúa de inmediato: retira y elimina fuera del campo las plantas muy afectadas y aplica un fungicida autorizado siguiendo la etiqueta, con asesoría de un ingeniero agrónomo. Avisa a tus vecinos porque se propaga rápido.",
            prevencion = "Usa semilla sana, mejora la ventilación y el drenaje, evita el riego por aspersión y revisa tus plantas después de las lluvias."
        ),
        Recomendacion(
            plagaClave = "Potato___healthy",
            nombre = "Planta sana",
            descripcion = "La planta no muestra signos de las enfermedades evaluadas.",
            nivelSeveridad = "NINGUNA",
            tratamiento = "No necesitas aplicar ningún tratamiento por ahora.",
            prevencion = "Sigue revisando tus plantas cada semana, sobre todo después de lluvias o días húmedos."
        )
    )

    val porClave: Map<String, Recomendacion> =
        recomendaciones.associateBy { it.plagaClave }

    val sinDatos = Recomendacion(
        plagaClave = "desconocido",
        nombre = "Sin identificar",
        descripcion = "No se pudo reconocer la enfermedad en la foto.",
        nivelSeveridad = "NINGUNA",
        tratamiento = "Toma otra foto con buena luz, acercando la hoja y sin objetos alrededor. Si el problema continúa, consulta a un ingeniero agrónomo.",
        prevencion = "Revisa tus cultivos con frecuencia."
    )
}