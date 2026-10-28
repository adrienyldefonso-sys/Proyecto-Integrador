package com.yldefonso.ojodchakra.screens.parcelas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ParcelasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ParcelaRepository(application)

    // null mientras Room responde la primera vez
    val parcelas: StateFlow<List<ParcelaConConteo>?> = repository.parcelas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}