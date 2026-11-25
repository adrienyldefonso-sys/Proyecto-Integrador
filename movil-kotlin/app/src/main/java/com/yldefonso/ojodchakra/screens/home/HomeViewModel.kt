package com.yldefonso.ojodchakra.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repoParcelas = ParcelaRepository(application)
    private val repoReportes = DiagnosticoRepository(application)

    // null mientras Room responde la primera vez
    val totalParcelas: StateFlow<Int?> = repoParcelas.parcelas
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalReportes: StateFlow<Int?> = repoReportes.diagnosticos
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}