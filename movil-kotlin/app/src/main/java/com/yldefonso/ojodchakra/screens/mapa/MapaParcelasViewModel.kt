package com.yldefonso.ojodchakra.screens.mapa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.model.ParcelaEnMapa
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MapaParcelasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ParcelaRepository(application)

    val parcelas: StateFlow<List<ParcelaEnMapa>> = repository.parcelasEnMapa.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
