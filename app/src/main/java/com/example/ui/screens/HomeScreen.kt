package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MathSolution
import com.example.ui.components.ScratchpadCanvas
import com.example.ui.components.SolutionStepCard
import com.example.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isLoading: Boolean,
    errorMessage: String?,
    currentSolution: MathSolution?,
    onSolveText: (String, Bitmap?) -> Unit,
    onSolveCanvas: (Bitmap) -> Unit,
    onBookmarkToggle: (Boolean) -> Unit,
    onSpeakClick: (String) -> Unit,
    onClearSolution: () -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Text, 1: Scratchpad, 2: Camera / Presets
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                selectedImageBitmap = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val sampleQuestions = listOf(
        "Solve 3x² - 12x + 9 = 0",
        "Find the derivative of f(x) = x³ · sin(x)",
        "Integral of ∫ (2x + 5) dx from 0 to 3",
        "A ladder 10ft long leans against a wall. If the bottom slides away at 2ft/s, how fast is the top sliding down when the bottom is 6ft from wall?",
        "If sin(θ) = 4/5 in quadrant I, find cos(2θ)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // App Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ AI Math Solver",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get instant step-by-step solutions with detailed explanations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Text(text = "🧮", fontSize = 36.sp)
                }
            }
        }

        // Mode Tabs: Type / Draw Canvas / Photo Import
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                indicator = { },
                divider = { }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("tab_type_problem"),
                    text = { Text("Type Text", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("tab_scratchpad"),
                    text = { Text("Draw Canvas", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("tab_photo_picker"),
                    text = { Text("Photo / Presets", fontWeight = FontWeight.SemiBold) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab Content
        item {
            when (selectedTab) {
                0 -> { // Type Text
                    Column {
                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("math_question_input"),
                            placeholder = {
                                Text("Type your math equation or word problem here...\ne.g., Solve 2x + 5 = 17 or Find limit as x -> 0 of sin(x)/x")
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick sample problem chips
                        Text(
                            text = "💡 Quick Sample Questions:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sampleQuestions) { sample ->
                                SuggestionChip(
                                    onClick = { questionText = sample },
                                    label = { Text(sample, maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Solve Action Button
                        Button(
                            onClick = {
                                if (questionText.isNotBlank()) {
                                    onSolveText(questionText, selectedImageBitmap)
                                }
                            },
                            enabled = questionText.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("solve_text_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color(0xFF0F172A),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Analyzing & Solving...", style = MaterialTheme.typography.titleMedium)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Solve Step-by-Step", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                1 -> { // Draw Scratchpad Canvas
                    ScratchpadCanvas(
                        onSolveCanvas = { bitmap ->
                            onSolveCanvas(bitmap)
                        }
                    )
                }

                2 -> { // Photo Import & Image Preset
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageBitmap != null) {
                                    Image(
                                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                                        contentDescription = "Selected Problem Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Upload Image",
                                            modifier = Modifier.size(40.dp),
                                            tint = NeonCyan
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Tap to Pick Image of Math Problem",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Supports printed or handwritten equations",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedImageBitmap != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(onClick = { selectedImageBitmap = null }) {
                                    Text("Remove Photo")
                                }

                                Button(
                                    onClick = {
                                        onSolveText(
                                            questionText.ifBlank { "Solve the problem in the attached image" },
                                            selectedImageBitmap
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF0F172A))
                                ) {
                                    Text("Solve Photo Problem")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error banner
        if (errorMessage != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Loading Indicator Banner when active
        if (isLoading) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "🧠 AI Engine is Reasoning...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Generating step-by-step breakdown & verification",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Display Generated Solution
        if (currentSolution != null && !isLoading) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Solution Result",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onClearSolution) {
                        Text("Clear Solution")
                    }
                }

                SolutionStepCard(
                    solution = currentSolution,
                    onBookmarkToggle = onBookmarkToggle,
                    onSpeakClick = onSpeakClick,
                    onAskFollowUp = { followUpText ->
                        onSolveText(followUpText, null)
                    },
                    onPracticeQuestionClick = { practice ->
                        questionText = practice
                        selectedTab = 0
                    }
                )
            }
        }
    }
}
