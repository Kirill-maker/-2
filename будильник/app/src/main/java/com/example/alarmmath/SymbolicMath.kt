package com.example.alarmmath

import kotlin.random.Random
import kotlin.math.PI

/**
 * Символьное представление математических выражений.
 * Используется для генерации интегралов через дифференцирование.
 */
sealed class Expr {
    // Константа
    data class Const(val value: Double) : Expr() {
        override fun toString() = when {
            value == value.toLong().toDouble() -> value.toLong().toString()
            else -> String.format("%.2f", value).trimEnd('0').trimEnd('.')
        }
    }
    
    // Переменная x
    object X : Expr() {
        override fun toString() = "x"
    }
    
    // Сумма
    data class Add(val left: Expr, val right: Expr) : Expr()
    
    // Разность
    data class Sub(val left: Expr, val right: Expr) : Expr()
    
    // Произведение
    data class Mul(val left: Expr, val right: Expr) : Expr()
    
    // Частное
    data class Div(val left: Expr, val right: Expr) : Expr()
    
    // Степень
    data class Pow(val base: Expr, val exp: Expr) : Expr()
    
    // sin(x)
    data class Sin(val arg: Expr) : Expr()
    
    // cos(x)
    data class Cos(val arg: Expr) : Expr()
    
    // e^x
    data class Exp(val arg: Expr) : Expr()
    
    // ln(x)
    data class Ln(val arg: Expr) : Expr()
}

/**
 * Вычисление значения выражения при заданном x
 */
object Evaluator {
    fun evaluate(expr: Expr, x: Double): Double = when (expr) {
        is Expr.Const -> expr.value
        is Expr.X -> x
        is Expr.Add -> evaluate(expr.left, x) + evaluate(expr.right, x)
        is Expr.Sub -> evaluate(expr.left, x) - evaluate(expr.right, x)
        is Expr.Mul -> evaluate(expr.left, x) * evaluate(expr.right, x)
        is Expr.Div -> evaluate(expr.left, x) / evaluate(expr.right, x)
        is Expr.Pow -> Math.pow(evaluate(expr.base, x), evaluate(expr.exp, x))
        is Expr.Sin -> kotlin.math.sin(evaluate(expr.arg, x))
        is Expr.Cos -> kotlin.math.cos(evaluate(expr.arg, x))
        is Expr.Exp -> kotlin.math.exp(evaluate(expr.arg, x))
        is Expr.Ln -> kotlin.math.ln(evaluate(expr.arg, x))
    }
}

/**
 * Символьное дифференцирование
 */
object Differentiator {
    
    fun differentiate(expr: Expr): Expr {
        return simplify(diff(expr))
    }
    
    private fun diff(expr: Expr): Expr = when (expr) {
        is Expr.Const -> Expr.Const(0.0)
        is Expr.X -> Expr.Const(1.0)
        
        is Expr.Add -> Expr.Add(diff(expr.left), diff(expr.right))
        is Expr.Sub -> Expr.Sub(diff(expr.left), diff(expr.right))
        
        // Правило произведения: (uv)' = u'v + uv'
        is Expr.Mul -> Expr.Add(
            Expr.Mul(diff(expr.left), expr.right),
            Expr.Mul(expr.left, diff(expr.right))
        )
        
        // Правило частного: (u/v)' = (u'v - uv') / v²
        is Expr.Div -> Expr.Div(
            Expr.Sub(
                Expr.Mul(diff(expr.left), expr.right),
                Expr.Mul(expr.left, diff(expr.right))
            ),
            Expr.Pow(expr.right, Expr.Const(2.0))
        )
        
        // Степенная функция: (x^n)' = n·x^(n-1) · x'
        is Expr.Pow -> {
            when {
                expr.exp is Expr.Const -> {
                    val n = expr.exp.value
                    Expr.Mul(
                        Expr.Mul(Expr.Const(n), Expr.Pow(expr.base, Expr.Const(n - 1))),
                        diff(expr.base)
                    )
                }
                else -> {
                    diff(Expr.Exp(Expr.Mul(expr.exp, Expr.Ln(expr.base))))
                }
            }
        }
        
        // (sin(u))' = cos(u)·u'
        is Expr.Sin -> Expr.Mul(Expr.Cos(expr.arg), diff(expr.arg))
        
        // (cos(u))' = -sin(u)·u'
        is Expr.Cos -> Expr.Mul(Expr.Const(-1.0), Expr.Mul(Expr.Sin(expr.arg), diff(expr.arg)))
        
        // (e^u)' = e^u · u'
        is Expr.Exp -> Expr.Mul(expr, diff(expr.arg))
        
        // (ln(u))' = u'/u
        is Expr.Ln -> Expr.Div(diff(expr.arg), expr.arg)
    }
    
    /**
     * Упрощение выражений
     */
    fun simplify(expr: Expr): Expr = when (expr) {
        is Expr.Const, is Expr.X -> expr
        
        is Expr.Add -> {
            val l = simplify(expr.left)
            val r = simplify(expr.right)
            when {
                l is Expr.Const && l.value == 0.0 -> r
                r is Expr.Const && r.value == 0.0 -> l
                l is Expr.Const && r is Expr.Const -> Expr.Const(l.value + r.value)
                else -> Expr.Add(l, r)
            }
        }
        
        is Expr.Sub -> {
            val l = simplify(expr.left)
            val r = simplify(expr.right)
            when {
                r is Expr.Const && r.value == 0.0 -> l
                l is Expr.Const && r is Expr.Const -> Expr.Const(l.value - r.value)
                else -> Expr.Sub(l, r)
            }
        }
        
        is Expr.Mul -> {
            val l = simplify(expr.left)
            val r = simplify(expr.right)
            when {
                l is Expr.Const && l.value == 0.0 -> Expr.Const(0.0)
                r is Expr.Const && r.value == 0.0 -> Expr.Const(0.0)
                l is Expr.Const && l.value == 1.0 -> r
                r is Expr.Const && r.value == 1.0 -> l
                l is Expr.Const && r is Expr.Const -> Expr.Const(l.value * r.value)
                l is Expr.Const && r is Expr.Mul && r.left is Expr.Const -> {
                    Expr.Mul(Expr.Const(l.value * r.left.value), simplify(r.right))
                }
                else -> Expr.Mul(l, r)
            }
        }
        
        is Expr.Div -> {
            val l = simplify(expr.left)
            val r = simplify(expr.right)
            when {
                l is Expr.Const && l.value == 0.0 -> Expr.Const(0.0)
                r is Expr.Const && r.value == 1.0 -> l
                l is Expr.Const && r is Expr.Const && r.value != 0.0 -> Expr.Const(l.value / r.value)
                else -> Expr.Div(l, r)
            }
        }
        
        is Expr.Pow -> {
            val b = simplify(expr.base)
            val e = simplify(expr.exp)
            when {
                e is Expr.Const && e.value == 0.0 -> Expr.Const(1.0)
                e is Expr.Const && e.value == 1.0 -> b
                b is Expr.Const && e is Expr.Const -> Expr.Const(Math.pow(b.value, e.value))
                else -> Expr.Pow(b, e)
            }
        }
        
        is Expr.Sin -> Expr.Sin(simplify(expr.arg))
        is Expr.Cos -> Expr.Cos(simplify(expr.arg))
        is Expr.Exp -> Expr.Exp(simplify(expr.arg))
        is Expr.Ln -> Expr.Ln(simplify(expr.arg))
    }
}

/**
 * Генератор случайных функций для интегрирования
 */
object FunctionGenerator {
    
    // Unicode символы для красивого отображения
    private val superscripts = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '-' to '⁻', '+' to '⁺'
    )
    
    private val subscripts = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '-' to '₋', '+' to '₊'
    )
    
    fun toSuperscript(s: String): String = s.map { superscripts[it] ?: it }.joinToString("")
    fun toSubscript(s: String): String = s.map { subscripts[it] ?: it }.joinToString("")
    
    /**
     * Генерирует случайную функцию заданной сложности
     */
    fun generate(complexity: Int): Expr {
        return when (complexity) {
            0 -> generateSimple()
            1 -> generateMedium()
            else -> generateComplex()
        }
    }
    
    /**
     * Генерирует функцию подходящую для определённого интеграла
     * (избегаем ln, деление на 0 и т.д.)
     */
    fun generateForDefinite(): Expr {
        return when (Random.nextInt(6)) {
            0 -> {
                // ax^n (полином)
                val a = Random.nextInt(1, 5).toDouble()
                val n = Random.nextInt(2, 5).toDouble()
                Expr.Mul(Expr.Const(a), Expr.Pow(Expr.X, Expr.Const(n)))
            }
            1 -> {
                // a·sin(x)
                val a = Random.nextInt(1, 4).toDouble()
                Expr.Mul(Expr.Const(a), Expr.Sin(Expr.X))
            }
            2 -> {
                // a·cos(x)
                val a = Random.nextInt(1, 4).toDouble()
                Expr.Mul(Expr.Const(a), Expr.Cos(Expr.X))
            }
            3 -> {
                // e^(ax) где a маленькое
                val a = Random.nextInt(1, 3).toDouble()
                Expr.Exp(Expr.Mul(Expr.Const(a), Expr.X))
            }
            4 -> {
                // ax² + bx + c
                val a = Random.nextInt(1, 4).toDouble()
                val b = Random.nextInt(1, 4).toDouble()
                Expr.Add(
                    Expr.Mul(Expr.Const(a), Expr.Pow(Expr.X, Expr.Const(2.0))),
                    Expr.Mul(Expr.Const(b), Expr.X)
                )
            }
            else -> {
                // x·sin(x)
                Expr.Mul(Expr.X, Expr.Sin(Expr.X))
            }
        }
    }
    
    // Лёгкий
    private fun generateSimple(): Expr {
        return when (Random.nextInt(5)) {
            0 -> {
                val a = Random.nextInt(2, 6).toDouble()
                val n = Random.nextInt(2, 5).toDouble()
                Expr.Mul(Expr.Const(a), Expr.Pow(Expr.X, Expr.Const(n)))
            }
            1 -> {
                val a = Random.nextInt(1, 5).toDouble()
                if (Random.nextBoolean()) {
                    Expr.Mul(Expr.Const(a), Expr.Sin(Expr.X))
                } else {
                    Expr.Mul(Expr.Const(a), Expr.Cos(Expr.X))
                }
            }
            2 -> {
                val a = Random.nextInt(1, 4).toDouble()
                Expr.Exp(Expr.Mul(Expr.Const(a), Expr.X))
            }
            3 -> {
                val a = Random.nextInt(1, 5).toDouble()
                val b = Random.nextInt(1, 5).toDouble()
                Expr.Add(
                    Expr.Mul(Expr.Const(a), Expr.Pow(Expr.X, Expr.Const(2.0))),
                    Expr.Mul(Expr.Const(b), Expr.X)
                )
            }
            else -> {
                Expr.Ln(Expr.X)
            }
        }
    }
    
    // Средний
    private fun generateMedium(): Expr {
        return when (Random.nextInt(5)) {
            0 -> {
                if (Random.nextBoolean()) {
                    Expr.Mul(Expr.X, Expr.Sin(Expr.X))
                } else {
                    Expr.Mul(Expr.X, Expr.Cos(Expr.X))
                }
            }
            1 -> Expr.Mul(Expr.X, Expr.Exp(Expr.X))
            2 -> {
                val a = Random.nextInt(1, 4).toDouble()
                val ax = Expr.Mul(Expr.Const(a), Expr.X)
                Expr.Mul(Expr.Sin(ax), Expr.Cos(ax))
            }
            3 -> Expr.Mul(Expr.Pow(Expr.X, Expr.Const(2.0)), Expr.Exp(Expr.X))
            else -> Expr.Mul(Expr.X, Expr.Ln(Expr.X))
        }
    }
    
    // Сложный
    private fun generateComplex(): Expr {
        return when (Random.nextInt(6)) {
            0 -> Expr.Mul(Expr.Pow(Expr.X, Expr.Const(2.0)), Expr.Sin(Expr.X))
            1 -> Expr.Mul(Expr.Exp(Expr.X), Expr.Sin(Expr.X))
            2 -> Expr.Mul(Expr.Pow(Expr.X, Expr.Const(3.0)), Expr.Exp(Expr.X))
            3 -> Expr.Mul(Expr.Sin(Expr.X), Expr.Sin(Expr.X))
            4 -> Expr.Mul(Expr.X, Expr.Mul(Expr.Sin(Expr.X), Expr.Sin(Expr.X)))
            else -> Expr.Mul(Expr.Exp(Expr.X), Expr.Cos(Expr.X))
        }
    }
    
    /**
     * Форматирует выражение для красивого отображения
     */
    fun format(expr: Expr): String {
        return formatExpr(Differentiator.simplify(expr))
    }
    
    private fun formatExpr(expr: Expr): String {
        return when (expr) {
            is Expr.Const -> {
                val v = expr.value
                when {
                    v == v.toLong().toDouble() -> v.toLong().toString()
                    else -> String.format("%.2f", v).trimEnd('0').trimEnd('.')
                }
            }
            is Expr.X -> "x"
            is Expr.Add -> {
                val l = formatExpr(expr.left)
                val r = formatExpr(expr.right)
                "$l + $r"
            }
            is Expr.Sub -> {
                val l = formatExpr(expr.left)
                val r = formatExpr(expr.right)
                "$l - $r"
            }
            is Expr.Mul -> {
                val l = formatExpr(expr.left)
                val r = formatExpr(expr.right)
                when {
                    expr.left is Expr.Const && expr.right is Expr.X -> "${l}x"
                    expr.left is Expr.Const && expr.left.value == 1.0 -> r
                    expr.right is Expr.Const && expr.right.value == 1.0 -> l
                    expr.left is Expr.Const && expr.left.value == -1.0 -> "-$r"
                    expr.left is Expr.Const -> "$l·$r"
                    else -> "$l·$r"
                }
            }
            is Expr.Div -> "(${formatExpr(expr.left)})/(${formatExpr(expr.right)})"
            is Expr.Pow -> {
                val b = formatExpr(expr.base)
                val e = expr.exp
                when {
                    e is Expr.Const && e.value == 2.0 -> "$b²"
                    e is Expr.Const && e.value == 3.0 -> "$b³"
                    e is Expr.Const && e.value == 4.0 -> "$b⁴"
                    e is Expr.Const && e.value == 5.0 -> "$b⁵"
                    e is Expr.Const && e.value == 0.5 -> "√$b"
                    e is Expr.Const && e.value == e.value.toLong().toDouble() -> 
                        "$b${toSuperscript(e.value.toLong().toString())}"
                    else -> "$b^(${formatExpr(e)})"
                }
            }
            is Expr.Sin -> "sin(${formatExpr(expr.arg)})"
            is Expr.Cos -> "cos(${formatExpr(expr.arg)})"
            is Expr.Exp -> {
                val a = expr.arg
                when {
                    a is Expr.X -> "eˣ"
                    a is Expr.Mul && a.left is Expr.Const && a.right is Expr.X -> {
                        val coef = a.left.value.toLong()
                        "e${toSuperscript("${coef}x")}"
                    }
                    else -> "e^(${formatExpr(a)})"
                }
            }
            is Expr.Ln -> "ln(${formatExpr(expr.arg)})"
        }
    }
    
    /**
     * Форматирование числа для ответа
     */
    fun formatNumber(value: Double): String {
        return when {
            value == value.toLong().toDouble() -> value.toLong().toString()
            kotlin.math.abs(value - kotlin.math.round(value)) < 0.001 -> 
                kotlin.math.round(value).toLong().toString()
            else -> String.format("%.2f", value)
        }
    }
}

/**
 * Красивые пределы интегрирования
 */
data class IntegralBounds(
    val lower: Double,
    val upper: Double,
    val lowerDisplay: String,
    val upperDisplay: String
)

object BoundsGenerator {
    private val commonBounds = listOf(
        IntegralBounds(0.0, 1.0, "0", "1"),
        IntegralBounds(0.0, 2.0, "0", "2"),
        IntegralBounds(1.0, 2.0, "1", "2"),
        IntegralBounds(0.0, 3.0, "0", "3"),
        IntegralBounds(1.0, 3.0, "1", "3"),
        IntegralBounds(0.0, PI, "0", "π"),
        IntegralBounds(0.0, PI / 2, "0", "π/2"),
        IntegralBounds(0.0, 2 * PI, "0", "2π"),
        IntegralBounds(-1.0, 1.0, "-1", "1"),
        IntegralBounds(0.0, 4.0, "0", "4"),
    )
    
    fun random(): IntegralBounds = commonBounds.random()
    
    fun randomForPolynomial(): IntegralBounds = listOf(
        IntegralBounds(0.0, 1.0, "0", "1"),
        IntegralBounds(0.0, 2.0, "0", "2"),
        IntegralBounds(1.0, 2.0, "1", "2"),
        IntegralBounds(0.0, 3.0, "0", "3"),
        IntegralBounds(1.0, 3.0, "1", "3"),
        IntegralBounds(-1.0, 1.0, "-1", "1"),
    ).random()
    
    fun randomForTrig(): IntegralBounds = listOf(
        IntegralBounds(0.0, PI, "0", "π"),
        IntegralBounds(0.0, PI / 2, "0", "π/2"),
        IntegralBounds(0.0, 2 * PI, "0", "2π"),
        IntegralBounds(-PI, PI, "-π", "π"),
    ).random()
}
