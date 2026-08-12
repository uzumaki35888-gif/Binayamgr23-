package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.FormulaScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.delay

enum class NavDestination(val label: String, val icon: ImageVector) {
    SOLVER("Solver", Icons.Default.AutoAwesome),
    ASSISTANT("AI Assistant", Icons.Default.SmartToy),
    CALCULATOR("Calculator", Icons.Default.Calculate),
    FORMULAS("Formulas", Icons.Default.Functions),
    HISTORY("History", Icons.Default.History)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MathViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    MainAppContent(viewModel)

                    AnimatedVisibility(
                        visible = showSplash,
                        exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(targetOffsetY = { -it })
                    ) {
                        AppSplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splash_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C29),
                        Color(0xFF1E1B4B),
                        Color(0xFF311B92)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            // Animated Lightbulb Icon Frame
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseGlow)
                    .clip(RoundedCornerShape(32.dp))
                    .background(GeoPrimary)
                    .border(2.dp, NeonCyan.copy(alpha = 0.8f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.math_app_icon_1786526193427),
                    contentDescription = "MathLens Lightbulb App Icon",
                    modifier = Modifier.size(105.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MathLens AI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Instant Geometric & Math Solver",
                style = MaterialTheme.typography.bodyMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = NeonCyan,
                strokeWidth = 2.5.dp
            )
        }

        // Developer Credit Banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DEVELOPED BY",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Binaya Pulami Magar",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MainAppContent(viewModel: MathViewModel) {
    var currentDestination by remember { mutableStateOf(NavDestination.SOLVER) }

    val historyList by viewModel.historyState.collectAsStateWithLifecycle()
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value
    val currentSolution = viewModel.currentSolution.value

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination == dest,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = NeonCyan,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(280)) + slideInHorizontally(
                        initialOffsetX = { if (targetState.ordinal > initialState.ordinal) it / 2 else -it / 2 },
                        animationSpec = tween(280)
                    ) togetherWith fadeOut(animationSpec = tween(220)) + slideOutHorizontally(
                        targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -it / 2 else it / 2 },
                        animationSpec = tween(220)
                    )
                },
                label = "smooth_tab_transition"
            ) { destination ->
                when (destination) {
                    NavDestination.SOLVER -> {
                        HomeScreen(
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            currentSolution = currentSolution,
                            onSolveText = { text, bitmap ->
                                viewModel.solveTextQuestion(text, bitmap)
                            },
                            onSolveCanvas = { bitmap ->
                                viewModel.solveCanvasDrawing(bitmap)
                            },
                            onBookmarkToggle = { isBookmarked ->
                                viewModel.toggleCurrentBookmark(isBookmarked)
                            },
                            onSpeakClick = { text ->
                                viewModel.speakText(text)
                            },
                            onClearSolution = {
                                viewModel.clearCurrentSolution()
                            }
                        )
                    }

                    NavDestination.ASSISTANT -> {
                        AssistantScreen(
                            chatMessages = viewModel.chatMessages,
                            isThinking = viewModel.isAssistantThinking.value,
                            onSendMessage = { prompt ->
                                viewModel.sendAssistantMessage(prompt)
                            },
                            onClearChat = {
                                viewModel.clearAssistantChat()
                            },
                            onSpeakClick = { text ->
                                viewModel.speakText(text)
                            },
                            onSendToSolver = { mathText ->
                                viewModel.solveTextQuestion(mathText)
                                currentDestination = NavDestination.SOLVER
                            }
                        )
                    }

                    NavDestination.CALCULATOR -> {
                        CalculatorScreen(
                            onSendToAISolver = { expr ->
                                viewModel.solveTextQuestion(expr)
                                currentDestination = NavDestination.SOLVER
                            }
                        )
                    }

                    NavDestination.FORMULAS -> {
                        FormulaScreen(
                            onSolveSampleQuestion = { sample ->
                                viewModel.solveTextQuestion(sample)
                                currentDestination = NavDestination.SOLVER
                            }
                        )
                    }

                    NavDestination.HISTORY -> {
                        HistoryScreen(
                            historyList = historyList,
                            onBookmarkToggle = { id, isBm ->
                                viewModel.toggleBookmark(id, isBm)
                            },
                            onDeleteQuestion = { id ->
                                viewModel.deleteQuestion(id)
                            },
                            onClearAll = {
                                viewModel.clearAllHistory()
                            },
                            onSpeakClick = { text ->
                                viewModel.speakText(text)
                            }
                        )
                    }
                }
            }
        }
    }
}

