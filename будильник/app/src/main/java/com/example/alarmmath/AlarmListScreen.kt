package com.example.alarmmath

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmListScreen(vm: AlarmListViewModel = viewModel()) {
    val alarms by vm.alarms.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                    selectedMinute = cal.get(Calendar.MINUTE)
                    showTimePicker = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить будильник")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Заголовок
            Text(
                text = "⏰ Будильники",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
            
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Нет будильников",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Нажмите + чтобы добавить",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { enabled -> vm.toggle(alarm, enabled) },
                            onDelete = { vm.delete(alarm) }
                        )
                    }
                }
            }
        }
    }

    // Диалог выбора времени и сложности
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, difficulty ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DATE, 1)
                    }
                }
                vm.add(cal.timeInMillis, difficulty)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, difficulty: Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }
    var selectedDifficulty by remember { mutableStateOf(IntegralGenerator.Difficulty.EASY) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Новый будильник",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(20.dp))
                
                // iOS-стиль wheel picker
                WheelTimePicker(
                    initialHour = initialHour,
                    initialMinute = initialMinute,
                    onTimeSelected = { h, m ->
                        hour = h
                        minute = m
                    }
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Выбор сложности
                Text(
                    text = "Сложность интегралов",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IntegralGenerator.Difficulty.values().forEach { difficulty ->
                        val isSelected = selectedDifficulty == difficulty
                        val color = when (difficulty) {
                            IntegralGenerator.Difficulty.EASY -> Color(0xFF4CAF50)
                            IntegralGenerator.Difficulty.MEDIUM -> Color(0xFFFF9800)
                            IntegralGenerator.Difficulty.HARD -> Color(0xFFF44336)
                        }
                        
                        OutlinedButton(
                            onClick = { selectedDifficulty = difficulty },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                                contentColor = color
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) color else color.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = difficulty.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Описание выбранной сложности
                Spacer(Modifier.height(12.dp))
                Text(
                    text = when (selectedDifficulty) {
                        IntegralGenerator.Difficulty.EASY -> "Простые интегралы: ∫kx dx, ∫sin(x) dx"
                        IntegralGenerator.Difficulty.MEDIUM -> "Определённые интегралы с числовым ответом"
                        IntegralGenerator.Difficulty.HARD -> "Замена переменной, интегрирование по частям"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Отмена")
                    }
                    
                    Button(
                        onClick = { onConfirm(hour, minute, selectedDifficulty.ordinal) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItem(alarm: AlarmEntity, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val difficulty = IntegralGenerator.Difficulty.values().getOrElse(alarm.difficulty) {
        IntegralGenerator.Difficulty.EASY
    }
    val difficultyColor = when (difficulty) {
        IntegralGenerator.Difficulty.EASY -> Color(0xFF4CAF50)
        IntegralGenerator.Difficulty.MEDIUM -> Color(0xFFFF9800)
        IntegralGenerator.Difficulty.HARD -> Color(0xFFF44336)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeFormat.format(Date(alarm.timeMillis)),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = if (alarm.enabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Показываем дату
                    val cal = Calendar.getInstance()
                    val alarmCal = Calendar.getInstance().apply { timeInMillis = alarm.timeMillis }
                    val isToday = cal.get(Calendar.DAY_OF_YEAR) == alarmCal.get(Calendar.DAY_OF_YEAR)
                    
                    Text(
                        text = if (isToday) "Сегодня" else "Завтра",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Бейдж сложности
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = difficultyColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = difficulty.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = difficultyColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
