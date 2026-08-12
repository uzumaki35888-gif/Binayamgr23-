package com.example.util

import kotlin.math.*

object MathEvaluator {

    fun evaluate(expression: String, isDegreeMode: Boolean = true): String {
        return try {
            val cleanExpr = sanitize(expression)
            if (cleanExpr.isBlank()) return ""
            val valResult = Parser(cleanExpr, isDegreeMode).parse()
            if (valResult.isNaN() || valResult.isInfinite()) {
                "Error"
            } else {
                formatResult(valResult)
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun sanitize(expr: String): String {
        return expr.lowercase()
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            .replace("−", "-")
            .replace(" ", "")
    }

    private fun formatResult(valRes: Double): String {
        if (abs(valRes - valRes.toLong()) < 1e-9) {
            return valRes.toLong().toString()
        }
        val str = String.format("%.8f", valRes).trimEnd('0').trimEnd('.')
        return if (str == "-0") "0" else str
    }

    private class Parser(val str: String, val isDegreeMode: Boolean) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected character: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = this.pos

            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, this.pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, this.pos)
                if (func == "pi") {
                    x = PI
                } else if (func == "e") {
                    x = E
                } else {
                    x = parseFactor()
                    val rad = if (isDegreeMode) Math.toRadians(x) else x
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(rad)
                        "cos" -> cos(rad)
                        "tan" -> tan(rad)
                        "asin" -> if (isDegreeMode) Math.toDegrees(asin(x)) else asin(x)
                        "acos" -> if (isDegreeMode) Math.toDegrees(acos(x)) else acos(x)
                        "atan" -> if (isDegreeMode) Math.toDegrees(atan(x)) else atan(x)
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        "abs" -> abs(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                }
            } else {
                throw RuntimeException("Unexpected char: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())
            if (eat('!'.code)) x = factorial(x)

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0) return Double.NaN
            val intN = n.toInt()
            var res = 1.0
            for (i in 2..intN) res *= i
            return res
        }
    }
}
