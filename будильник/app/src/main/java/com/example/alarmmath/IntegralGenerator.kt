package com.example.alarmmath

import kotlin.math.pow
import kotlin.random.Random

/**
 * Generates simple integral questions with 4 multiple-choice options.
 * Difficulty levels:
 * 0 – very easy (∫ k dx)
 * 1 – easy (∫ kx dx)
 * 2 – medium (∫ kx^n dx, n≤3)
 * 3 – hard (trig integrals like ∫ cos(kx) dx)
 */
object IntegralGenerator {
    data class Question(
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int
    )

    fun random(level: Int = 0): Question {
        return when (level) {
            0 -> genConstant()
            1 -> genLinear()
            2 -> genPower()
            else -> genTrig()
        }
    }

    private fun genConstant(): Question {
        val k = Random.nextInt(1, 9)
        val correct = "$k x"
        val opts = shuffledOptions(correct, listOf(
            "${k}x + C", // distractor with +C
            "${k + 1}x",
            "${k}/x",
            "${k}"
        ))
        return Question("∫ $k dx = ?", opts.first, opts.second)
    }

    private fun genLinear(): Question {
        val k = Random.nextInt(1, 6)
        val correct = "${k}/2 x²"
        val opts = shuffledOptions(correct, listOf(
            "${k}x²",
            "${k}x",
            "x²/${k}",
            "${k}/3 x³"
        ))
        return Question("∫ ${k}x dx = ?", opts.first, opts.second)
    }

    private fun genPower(): Question {
        val k = Random.nextInt(1, 5)
        val n = Random.nextInt(2, 4)
        val correct = "${k}/${n + 1} x^{${n + 1}}"
        val opts = shuffledOptions(correct, listOf(
            "${k}x^{${n + 1}}",
            "${k}/${n} x^{${n}}",
            "${k}/${n + 2} x^{${n + 2}}",
            "${k}x^{${n}}"
        ))
        return Question("∫ ${k}x^$n dx = ?", opts.first, opts.second)
    }

    private fun genTrig(): Question {
        val k = Random.nextInt(1, 4)
        val correct = "(1/$k) sin(${k}x)"
        val opts = shuffledOptions(correct, listOf(
            "cos(${k}x)/$k",
            "(1/$k) cos(${k}x)",
            "sin(${k}x)*$k",
            "tan(${k}x)/$k"
        ))
        return Question("∫ cos(${k}x) dx = ?", opts.first, opts.second)
    }

    private fun shuffledOptions(correct: String, distractors: List<String>): Pair<List<String>, Int> {
        val mix = distractors.toMutableList().apply { set(0, correct) }.shuffled()
        val idx = mix.indexOf(correct)
        return Pair(mix, idx)
    }
}
