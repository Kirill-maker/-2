package com.example.alarmmath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    var scheduled by remember { mutableStateOf(false) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = {
                            AlarmScheduler.scheduleExact(this@MainActivity, 10_000) // 10 seconds
                            scheduled = true
                        }) {
                            Text("Set alarm in 10 seconds")
                        }
                        if (scheduled) Spacer(Modifier.height(8.dp))
                        if (scheduled) Text("Alarm scheduled 🎉")
                    }
                }
            }
        }
    }
}
