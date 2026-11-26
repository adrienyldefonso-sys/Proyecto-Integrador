package com.yldefonso.ojodchakra.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import com.yldefonso.ojodchakra.reporte.ReportePdf
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiagnosticoRepository(application)
    private val parcelaRepository = ParcelaRepository(application)

    // Genera el PDF de una página con los datos del reporte
    suspend fun generarPdf(diagnostico: Diagnostico): File {
        val nombreParcela = diagnostico.parcelaId?.let { parcelaRepository.obtenerPorId(it)?.nombre }
        return withContext(Dispatchers.IO) {
            ReportePdf.generar(getApplication(), diagnostico, nombreParcela)
        }
    }

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