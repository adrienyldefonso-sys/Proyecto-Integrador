package com.yldefonso.ojodchakra.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiagnosticoRepository(application)

    val diagnosticos: StateFlow<List<Diagnostico>> = repository.diagnosticos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun observarDiagnostico(id: String): Flow<Diagnostico?> =
        repository.observarPorId(id)

    fun borrarHistorial() {
        viewModelScope.launch {
            repository.limpiarHistorial()
        }
    }
}