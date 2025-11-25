package com.example.alarmmath

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playRingtone()
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                QuizScreen(onSolved = {
                    stopRingtone()
                    finish()
                })
            }
        }
    }

    private fun playRingtone() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uri).apply { isLooping = true; play() }
    }

    private fun stopRingtone() {
        ringtone?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}

@Composable
fun QuizScreen(level: Int = 1, onSolved: () -> Unit) {
    var question by remember { mutableStateOf(IntegralGenerator.random(level)) }
    var message by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = question.prompt, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            question.options.forEachIndexed { index, option ->
                Button(onClick = {
                    if (index == question.correctIndex) {
                        message = "Верно! Будильник остановлен"
                        onSolved()
                    } else {
                        message = "Неправильно! Попробуйте снова"
                    }
                }, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(option)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(message)
        }
    }
}
