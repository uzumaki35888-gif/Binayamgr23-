package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.util.MathEvaluator

@Composable
fun ScientificCalculatorView(
    modifier: Modifier = Modifier,
    onSendToAISolver: (String) -> Unit = {}
) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isDegreeMode by remember { mutableStateOf(true) }
    var isScientificMode by remember { mutableStateOf(true) }

    val basicKeys = listOf(
        "C", "(", ")", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "−",
        "1", "2", "3", "+",
        "0", ".", "DEL", "="
    )

    val scientificKeys = listOf(
        "sin", "cos", "tan", "^",
        "sqrt", "log", "ln", "π",
        "e", "abs", "%", "RAD/DEG"
    )

    fun appendInput(str: String) {
        when (str) {
            "C" -> {
                expression = ""
                result = ""
            }
            "DEL" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                if (expression.isNotBlank()) {
                    val evalRes = MathEvaluator.evaluate(expression, isDegreeMode)
                    result = evalRes
                }
            }
            "RAD/DEG" -> {
                isDegreeMode = !isDegreeMode
            }
            "sin", "cos", "tan", "sqrt", "log", "ln", "abs" -> {
                expression += "$str("
            }
            else -> {
                expression += str
            }
        }
        if (str != "=" && expression.isNotBlank()) {
            val quickEval = MathEvaluator.evaluate(expression, isDegreeMode)
            if (quickEval != "Error") {
                result = quickEval
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // Mode Switcher: Deg/Rad Indicator & Scientific Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = isDegreeMode,
                onClick = { isDegreeMode = !isDegreeMode },
                label = { Text(if (isDegreeMode) "DEG" else "RAD", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan)
            )

            TextButton(onClick = { isScientificMode = !isScientificMode }) {
                Text(
                    text = if (isScientificMode) "Basic Pad" else "Scientific Pad",
                    color = NeonCyan,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF020617))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                // Expression Input Line
                Text(
                    text = expression.ifBlank { "0" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Calculated Result Line
                Text(
                    text = if (result.isNotBlank()) "= $result" else "",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ask AI Solver Button for step-by-step breakdown
        if (expression.isNotBlank()) {
            Button(
                onClick = { onSendToAISolver("Solve this equation step-by-step: $expression") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("ai_step_by_step_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Step-by-Step AI Solution", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scientific Keys Row
        if (isScientificMode) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(scientificKeys) { key ->
                    CalcKeyButton(
                        text = key,
                        isAccent = false,
                        isOperator = true,
                        onClick = { appendInput(key) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Basic Keypad
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(basicKeys) { key ->
                val isOp = key in listOf("÷", "×", "−", "+", "=", "%", "^")
                val isAction = key in listOf("C", "DEL")
                CalcKeyButton(
                    text = key,
                    isAccent = key == "=",
                    isOperator = isOp,
                    isAction = isAction,
                    onClick = { appendInput(key) }
                )
            }
        }
    }
}

@Composable
fun CalcKeyButton(
    text: String,
    isAccent: Boolean = false,
    isOperator: Boolean = false,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isAccent -> NeonCyan
        isAction -> MaterialTheme.colorScheme.errorContainer
        isOperator -> Color(0xFF1E293B)
        else -> Color(0xFF334155)
    }

    val textColor = when {
        isAccent -> Color(0xFF020617)
        isAction -> MaterialTheme.colorScheme.onErrorContainer
        isOperator -> NeonCyan
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .testTag("calc_key_$text"),
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                fontSize = if (text.length > 3) 12.sp else 18.sp
            )
        }
    }
}
