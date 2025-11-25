package com.example.alarmmath

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Получаем сложность из intent
        val difficultyOrdinal = intent.getIntExtra(AlarmScheduler.EXTRA_DIFFICULTY, 0)
        val difficulty = IntegralGenerator.Difficulty.values().getOrElse(difficultyOrdinal) { 
            IntegralGenerator.Difficulty.EASY 
        }
        
        // Показать поверх экрана блокировки
        showOnLockScreen()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmQuizScreen(
                        difficulty = difficulty,
                        totalQuestions = 3,
                        onAllSolved = {
                            // Останавливаем сервис со звуком
                            AlarmService.stop(this@AlarmActivity)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Не даём закрыть кнопкой назад пока не решены все интегралы
    }
}

@Composable
fun AlarmQuizScreen(
    difficulty: IntegralGenerator.Difficulty,
    totalQuestions: Int = 3,
    onAllSolved: () -> Unit
) {
    var solvedCount by remember { mutableStateOf(0) }
    var question by remember { mutableStateOf(IntegralGenerator.generate(difficulty)) }
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Unspecified) }

    // Цвет в зависимости от сложности
    val difficultyColor = when (difficulty) {
        IntegralGenerator.Difficulty.EASY -> Color(0xFF4CAF50)    // Зелёный
        IntegralGenerator.Difficulty.MEDIUM -> Color(0xFFFF9800)  // Оранжевый
        IntegralGenerator.Difficulty.HARD -> Color(0xFFF44336)    // Красный
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = "🔔 Будильник!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))
                
                // Уровень сложности
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = difficultyColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = difficulty.displayName,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = difficultyColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Прогресс
                Text(
                    text = "Решено: $solvedCount / $totalQuestions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Прогресс-бар
                LinearProgressIndicator(
                    progress = solvedCount.toFloat() / totalQuestions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(8.dp),
                    color = difficultyColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                Spacer(Modifier.height(12.dp))

                // Вопрос
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Варианты ответов
                question.options.forEachIndexed { index, option ->
                    Button(
                        onClick = {
                            if (index == question.correctIndex) {
                                solvedCount++
                                if (solvedCount >= totalQuestions) {
                                    message = "🎉 Все решено! Доброе утро!"
                                    messageColor = Color(0xFF4CAF50)
                                    onAllSolved()
                                } else {
                                    message = "✅ Верно! Осталось: ${totalQuestions - solvedCount}"
                                    messageColor = Color(0xFF4CAF50)
                                    question = IntegralGenerator.generate(difficulty)
                                }
                            } else {
                                message = "❌ Неверно! Попробуй ещё"
                                messageColor = Color(0xFFF44336)
                                question = IntegralGenerator.generate(difficulty)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Сообщение
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = messageColor,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
