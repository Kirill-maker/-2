package com.example.alarmmath

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    initialHour: Int = 7,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val visibleItems = 5
    val itemHeight = 50.dp
    
    // Множитель для "бесконечной" прокрутки
    val multiplier = 1000
    val hourRange = 24
    val minuteRange = 60
    
    val hourListState = rememberLazyListState(
        initialFirstVisibleItemIndex = multiplier * hourRange / 2 + initialHour - visibleItems / 2
    )
    val minuteListState = rememberLazyListState(
        initialFirstVisibleItemIndex = multiplier * minuteRange / 2 + initialMinute - visibleItems / 2
    )
    
    val scope = rememberCoroutineScope()
    
    // Вычисляем текущие значения
    val currentHour by remember {
        derivedStateOf {
            val centerIndex = hourListState.firstVisibleItemIndex + visibleItems / 2
            centerIndex % hourRange
        }
    }
    
    val currentMinute by remember {
        derivedStateOf {
            val centerIndex = minuteListState.firstVisibleItemIndex + visibleItems / 2
            centerIndex % minuteRange
        }
    }
    
    // Сообщаем об изменении времени
    LaunchedEffect(currentHour, currentMinute) {
        onTimeSelected(currentHour, currentMinute)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center
    ) {
        // Выделение центрального элемента
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(itemHeight)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Часы
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(itemHeight * visibleItems),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    state = hourListState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = hourListState)
                ) {
                    items(hourRange * multiplier) { index ->
                        val hour = index % hourRange
                        val centerOffset = index - (hourListState.firstVisibleItemIndex + visibleItems / 2)
                        val alpha = 1f - (abs(centerOffset) * 0.25f).coerceIn(0f, 0.7f)
                        val scale = 1f - (abs(centerOffset) * 0.1f).coerceIn(0f, 0.3f)
                        
                        Box(
                            modifier = Modifier
                                .height(itemHeight)
                                .fillMaxWidth()
                                .alpha(alpha),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", hour),
                                fontSize = (24 * scale).sp,
                                fontWeight = if (centerOffset == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (centerOffset == 0) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Разделитель
            Text(
                text = ":",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Минуты
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(itemHeight * visibleItems),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    state = minuteListState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = minuteListState)
                ) {
                    items(minuteRange * multiplier) { index ->
                        val minute = index % minuteRange
                        val centerOffset = index - (minuteListState.firstVisibleItemIndex + visibleItems / 2)
                        val alpha = 1f - (abs(centerOffset) * 0.25f).coerceIn(0f, 0.7f)
                        val scale = 1f - (abs(centerOffset) * 0.1f).coerceIn(0f, 0.3f)
                        
                        Box(
                            modifier = Modifier
                                .height(itemHeight)
                                .fillMaxWidth()
                                .alpha(alpha),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", minute),
                                fontSize = (24 * scale).sp,
                                fontWeight = if (centerOffset == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (centerOffset == 0) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

