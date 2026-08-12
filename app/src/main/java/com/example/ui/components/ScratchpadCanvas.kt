package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan

data class PathState(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchpadCanvas(
    modifier: Modifier = Modifier,
    onSolveCanvas: (Bitmap) -> Unit
) {
    var paths by remember { mutableStateOf(listOf<PathState>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentColor by remember { mutableStateOf(Color(0xFF00F5D4)) } // Neon Cyan default
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }

    val canvasBackgroundColor = Color(0xFF0F172A)

    val density = LocalDensity.current
    var canvasWidthPx by remember { mutableIntStateOf(1080) }
    var canvasHeightPx by remember { mutableIntStateOf(1080) }

    val colorsList = listOf(
        Color(0xFF00F5D4), // Cyan
        Color(0xFFFFD166), // Yellow
        Color(0xFFF72585), // Neon Pink
        Color(0xFF4CC9F0), // Sky Blue
        Color(0xFFFFFFFF)  // White
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(canvasBackgroundColor)
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        // Scratchpad Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ Math Scratchpad",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontSize = 16.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Eraser Toggle
                IconButton(
                    onClick = { isEraser = !isEraser },
                    modifier = Modifier.testTag("eraser_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Eraser",
                        tint = if (isEraser) NeonCyan else Color.Gray
                    )
                }

                // Undo
                IconButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            paths = paths.dropLast(1)
                        }
                    },
                    enabled = paths.isNotEmpty(),
                    modifier = Modifier.testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (paths.isNotEmpty()) Color.White else Color.DarkGray
                    )
                }

                // Clear
                IconButton(
                    onClick = {
                        paths = emptyList()
                        currentPath = null
                    },
                    modifier = Modifier.testTag("clear_canvas_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clear Canvas",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Color selector bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Color:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            colorsList.forEach { col ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(col)
                        .border(
                            width = if (!isEraser && currentColor == col) 2.5.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .border(
                            width = if (!isEraser && currentColor == col) 1.dp else 0.dp,
                            color = Color.Black,
                            shape = CircleShape
                        )
                        .pointerInput(col) {
                            detectDragGestures { _, _ -> }
                        }
                        .background(col)
                ) {
                    IconButton(
                        onClick = {
                            currentColor = col
                            isEraser = false
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        }

        // Interactive Drawing Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF020617))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .pointerInput(isEraser, currentColor, strokeWidth) {
                    canvasWidthPx = size.width
                    canvasHeightPx = size.height
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = newPath
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let {
                                paths = paths + PathState(
                                    path = it,
                                    color = if (isEraser) canvasBackgroundColor else currentColor,
                                    strokeWidth = if (isEraser) strokeWidth * 3f else strokeWidth,
                                    isEraser = isEraser
                                )
                            }
                            currentPath = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Grid guide lines for easy math writing
                val gridSpacing = 40.dp.toPx()
                var y = gridSpacing
                while (y < size.height) {
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridSpacing
                }

                // Render saved paths
                paths.forEach { pathState ->
                    drawPath(
                        path = pathState.path,
                        color = pathState.color,
                        style = Stroke(
                            width = pathState.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Render actively drawing path
                currentPath?.let { path ->
                    drawPath(
                        path = path,
                        color = if (isEraser) canvasBackgroundColor else currentColor,
                        style = Stroke(
                            width = if (isEraser) strokeWidth * 3f else strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (paths.isEmpty() && currentPath == null) {
                Text(
                    text = "✍️ Write equation or draw diagram here...",
                    color = Color.Gray.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Button: Solve Canvas Drawing
        Button(
            onClick = {
                val bitmap = renderCanvasToBitmap(paths, canvasWidthPx, canvasHeightPx)
                onSolveCanvas(bitmap)
            },
            enabled = paths.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("solve_scratchpad_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = Color(0xFF0F172A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Solve Handwritten Equation", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Render paths to a high-resolution Bitmap for Gemini Vision
private fun renderCanvasToBitmap(
    paths: List<PathState>,
    widthPx: Int,
    heightPx: Int
): Bitmap {
    val safeWidth = if (widthPx <= 0) 800 else widthPx
    val safeHeight = if (heightPx <= 0) 800 else heightPx
    val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    // Fill dark background
    canvas.drawColor(android.graphics.Color.parseColor("#0F172A"))

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    paths.forEach { pathState ->
        paint.color = pathState.color.toArgb()
        paint.strokeWidth = pathState.strokeWidth
        canvas.drawPath(pathState.path.asAndroidPath(), paint)
    }

    return bitmap
}
