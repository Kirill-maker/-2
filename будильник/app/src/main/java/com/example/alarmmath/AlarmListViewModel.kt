package com.example.alarmmath

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AlarmRepository.get(app)

    val alarms: StateFlow<List<AlarmEntity>> = repo.alarms
        .map { it.sortedBy { a -> a.timeMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(timeMillis: Long, difficulty: Int = 0) = viewModelScope.launch { 
        repo.add(timeMillis, difficulty) 
    }
    
    fun toggle(alarm: AlarmEntity, enabled: Boolean) = viewModelScope.launch { 
        repo.toggle(alarm, enabled) 
    }
    
    fun delete(alarm: AlarmEntity) = viewModelScope.launch { 
        repo.delete(alarm) 
    }
}
