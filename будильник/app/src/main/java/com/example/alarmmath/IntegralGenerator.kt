package com.example.alarmmath

import kotlin.random.Random

/**
 * Генератор интегралов через символьное дифференцирование.
 * 
 * EASY: Неопределённые интегралы (простые)
 * MEDIUM: Определённые интегралы с числовым ответом
 * HARD: Неопределённые интегралы (сложные)
 */
object IntegralGenerator {
    
    enum class Difficulty(val displayName: String) {
        EASY("Лёгкий"),
        MEDIUM("Средний"),
        HARD("Сложный")
    }
    
    data class Question(
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int
    )

    /**
     * Генерирует вопрос заданной сложности
     */
    fun generate(difficulty: Difficulty): Question {
        return when (difficulty) {
            Difficulty.EASY -> generateIndefiniteEasy()
            Difficulty.MEDIUM -> generateDefinite()
            Difficulty.HARD -> generateIndefiniteHard()
        }
    }
    
    /**
     * Лёгкий: простые неопределённые интегралы
     */
    private fun generateIndefiniteEasy(): Question {
        val antiderivative = FunctionGenerator.generate(0)
        val derivative = Differentiator.differentiate(antiderivative)
        
        val integrand = FunctionGenerator.format(derivative)
        val correctAnswer = FunctionGenerator.format(antiderivative)
        
        val distractors = generateDistractors(antiderivative, 0)
        val prompt = "∫ ($integrand) dx = ?"
        val options = shuffledOptions(correctAnswer, distractors)
        
        return Question(prompt, options.first, options.second)
    }
    
    /**
     * Средний: определённые интегралы с числовым ответом
     */
    private fun generateDefinite(): Question {
        // Генерируем функцию подходящую для определённого интеграла
        val antiderivative = FunctionGenerator.generateForDefinite()
        val derivative = Differentiator.differentiate(antiderivative)
        
        // Выбираем подходящие пределы
        val bounds = selectBoundsForFunction(antiderivative)
        
        // Вычисляем значение: F(b) - F(a)
        val upperValue = Evaluator.evaluate(antiderivative, bounds.upper)
        val lowerValue = Evaluator.evaluate(antiderivative, bounds.lower)
        val integralValue = upperValue - lowerValue
        
        // Форматируем
        val integrand = FunctionGenerator.format(derivative)
        val correctAnswer = FunctionGenerator.formatNumber(integralValue)
        
        // Красивое отображение пределов с Unicode
        val lowerSub = FunctionGenerator.toSubscript(bounds.lowerDisplay)
        val upperSup = FunctionGenerator.toSuperscript(bounds.upperDisplay)
        
        val prompt = "∫$lowerSub$upperSup ($integrand) dx = ?"
        
        // Генерируем отвлекающие числовые варианты
        val distractors = generateNumericDistractors(integralValue)
        val options = shuffledOptions(correctAnswer, distractors)
        
        return Question(prompt, options.first, options.second)
    }
    
    /**
     * Сложный: сложные неопределённые интегралы
     */
    private fun generateIndefiniteHard(): Question {
        val antiderivative = FunctionGenerator.generate(2)
        val derivative = Differentiator.differentiate(antiderivative)
        
        val integrand = FunctionGenerator.format(derivative)
        val correctAnswer = FunctionGenerator.format(antiderivative)
        
        val distractors = generateDistractors(antiderivative, 2)
        val prompt = "∫ ($integrand) dx = ?"
        val options = shuffledOptions(correctAnswer, distractors)
        
        return Question(prompt, options.first, options.second)
    }
    
    /**
     * Выбирает подходящие пределы для функции
     */
    private fun selectBoundsForFunction(expr: Expr): IntegralBounds {
        // Проверяем тип функции
        val hasTrig = containsTrig(expr)
        val hasLn = containsLn(expr)
        
        return when {
            hasTrig -> BoundsGenerator.randomForTrig()
            hasLn -> IntegralBounds(1.0, 2.0, "1", "2") // ln(x) определён для x > 0
            else -> BoundsGenerator.randomForPolynomial()
        }
    }
    
    private fun containsTrig(expr: Expr): Boolean = when (expr) {
        is Expr.Sin, is Expr.Cos -> true
        is Expr.Add -> containsTrig(expr.left) || containsTrig(expr.right)
        is Expr.Sub -> containsTrig(expr.left) || containsTrig(expr.right)
        is Expr.Mul -> containsTrig(expr.left) || containsTrig(expr.right)
        is Expr.Div -> containsTrig(expr.left) || containsTrig(expr.right)
        is Expr.Pow -> containsTrig(expr.base)
        is Expr.Exp -> containsTrig(expr.arg)
        is Expr.Ln -> containsTrig(expr.arg)
        else -> false
    }
    
    private fun containsLn(expr: Expr): Boolean = when (expr) {
        is Expr.Ln -> true
        is Expr.Add -> containsLn(expr.left) || containsLn(expr.right)
        is Expr.Sub -> containsLn(expr.left) || containsLn(expr.right)
        is Expr.Mul -> containsLn(expr.left) || containsLn(expr.right)
        is Expr.Div -> containsLn(expr.left) || containsLn(expr.right)
        is Expr.Pow -> containsLn(expr.base)
        is Expr.Exp -> containsLn(expr.arg)
        else -> false
    }
    
    /**
     * Генерирует числовые отвлекающие варианты
     */
    private fun generateNumericDistractors(correct: Double): List<String> {
        val distractors = mutableListOf<String>()
        
        // Немного больше/меньше
        distractors.add(FunctionGenerator.formatNumber(correct * 1.5))
        distractors.add(FunctionGenerator.formatNumber(correct * 2))
        distractors.add(FunctionGenerator.formatNumber(correct + Random.nextDouble(1.0, 5.0)))
        
        // Противоположный знак
        if (correct != 0.0) {
            distractors.add(FunctionGenerator.formatNumber(-correct))
        }
        
        // Случайные близкие числа
        distractors.add(FunctionGenerator.formatNumber(correct + Random.nextInt(-3, 4)))
        
        return distractors.filter { it != FunctionGenerator.formatNumber(correct) }
            .distinct()
            .take(3)
    }
    
    /**
     * Генерирует отвлекающие варианты для неопределённых интегралов
     */
    private fun generateDistractors(correct: Expr, complexity: Int): List<String> {
        val distractors = mutableListOf<String>()
        
        // 1. Производная вместо первообразной
        val derivative = Differentiator.differentiate(correct)
        distractors.add(FunctionGenerator.format(derivative))
        
        // 2. Изменённый коэффициент
        val modified = modifyCoefficient(correct)
        distractors.add(FunctionGenerator.format(modified))
        
        // 3. Другая функция
        val other = FunctionGenerator.generate(complexity)
        distractors.add(FunctionGenerator.format(other))
        
        // 4. Отрицательная функция
        val negated = Expr.Mul(Expr.Const(-1.0), correct)
        distractors.add(FunctionGenerator.format(negated))
        
        val correctStr = FunctionGenerator.format(correct)
        return distractors.filter { it != correctStr }.distinct().take(3)
    }
    
    private fun modifyCoefficient(expr: Expr): Expr {
        return when (expr) {
            is Expr.Mul -> {
                when (expr.left) {
                    is Expr.Const -> {
                        val newCoeff = expr.left.value * Random.nextDouble(1.5, 3.0)
                        Expr.Mul(Expr.Const(kotlin.math.round(newCoeff)), expr.right)
                    }
                    else -> Expr.Mul(Expr.Const(2.0), expr)
                }
            }
            is Expr.Add -> Expr.Add(modifyCoefficient(expr.left), expr.right)
            else -> Expr.Mul(Expr.Const(2.0), expr)
        }
    }
    
    private fun shuffledOptions(correct: String, distractors: List<String>): Pair<List<String>, Int> {
        val uniqueDistractors = distractors.filter { it != correct }.take(3)
        val allOptions = (listOf(correct) + uniqueDistractors).shuffled()
        val idx = allOptions.indexOf(correct)
        return Pair(allOptions, idx)
    }
    
    // Для обратной совместимости
    fun random(level: Int = 0): Question {
        val difficulty = Difficulty.values().getOrElse(level) { Difficulty.EASY }
        return generate(difficulty)
    }
}
