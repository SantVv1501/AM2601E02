// Archivo: MainActivity.kt (Implementación Final de la Fase 2)
package com.example.proyectofinal261e02

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.proyectofinal261e02.ui.theme.ProyectoFinal261E02Theme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.example.proyectofinal261e02.shake
import androidx.lifecycle.lifecycleScope // Para el scope del Coroutine
import androidx.compose.foundation.background // Necesario para el color de la Surface


// Clase Helper para inyectar el ViewModel (necesario solo si el ViewModel requiere parámetros, pero lo mantenemos simple)
class SimpleViewModelFactory(private val viewModelInstance: ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModelInstance as T
    }
}

// Definimos los estados de la aplicación para la navegación
sealed class Screen {
    object Loading : Screen()
    object Welcome : Screen()
    object Quiz : Screen()
    data class Results(val score: Int) : Screen()
}

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    // El ViewModel se inicializa una vez aquí, fuera del Composable, para persistir el estado durante la navegación.
    private var quizViewModel = QuizViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProyectoFinal261E02Theme {
                AppNavigation(quizViewModel)
            }
        }
    }

    // Navegador principal
    @Composable
    fun AppNavigation(quizViewModel: QuizViewModel) {
        // La pantalla inicial se queda en Loading por defecto
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }

        // El LaunchedEffect sólo maneja el delay inicial de la pantalla de carga
        LaunchedEffect(Unit) {
            delay(2000)
            currentScreen = Screen.Welcome
        }

        // Lógica de navegación
        when (currentScreen) {
            is Screen.Loading -> SplashScreen()
            is Screen.Welcome -> WelcomeScreen(
                onStartQuiz = {
                    // El ViewModel ya está listo, solo navegamos.
                    currentScreen = Screen.Quiz
                },
                quizViewModel = quizViewModel // <-- ¡NUEVA LÍNEA! Pasamos el VM.
            )
            is Screen.Quiz -> QuizScreen(
                quizViewModel = quizViewModel, // Pasamos la instancia existente del ViewModel
                onQuizFinished = { finalScore ->
                    currentScreen = Screen.Results(finalScore)
                },
                onBack = {
                    currentScreen = Screen.Welcome
                }
            )
            is Screen.Results -> ResultScreen(
                score = (currentScreen as Screen.Results).score,
                onRestart = {
                    // 1. Reseteamos el ViewModel (esto baraja las preguntas y resetea el score)
                    quizViewModel.resetQuiz()

                    // 2. Volvemos al inicio (WelcomeScreen)
                    currentScreen = Screen.Welcome
                }
            )
        }
    }
}

// ----------------------------------------------------------------------
// FUNCIONES COMPOSABLE DE PANTALLA
// ----------------------------------------------------------------------

@Composable
fun SplashScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ProyectoFinal261E02", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

// DENTRO DE MainActivity.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onStartQuiz: () -> Unit, quizViewModel: QuizViewModel) { // <-- ¡Recibe el ViewModel!

    // Estado para mostrar mensajes de feedback (ej: "Primero realice el test")
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Observa el estado del Quiz para saber si ha terminado (0=En Curso, 1=Terminado)
    val quizState by quizViewModel.quizState.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Prueba de Inglés Básico") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Título
            Text(
                "Bienvenido/a al Examen Básico",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 32.dp)
            )

            // Contenedor de Botones
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // 1. Botón de INICIO (Inicia la prueba)
                Button(
                    onClick = {
                        // Reiniciamos el ViewModel antes de iniciar la nueva prueba
                        if (quizState == 1) {
                            quizViewModel.resetQuiz() // Si ya terminó, resetea
                        }
                        onStartQuiz()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Iniciar Prueba Básica de Inglés", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Botón de REINICIO (Implementa la lógica de verificación)
                Button(
                    onClick = {
                        if (quizState != 1) {
                            // Lógica de Error
                            // ...
                        } else {
                            // Lógica de Éxito: Resetea y confirma
                            quizViewModel.resetQuiz()
                            feedbackMessage = "Test reiniciado correctamente."

                            // Reemplaza la línea que causa error con la nueva función:
                            quizViewModel.setQuizState(0) // <--- ¡LA LÍNEA CORREGIDA!
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Reiniciar Prueba", style = MaterialTheme.typography.titleMedium)
                }

                // 3. Mensaje de Feedback (Rojo/Verde)
                if (feedbackMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        feedbackMessage!!,
                        color = if (feedbackMessage!!.contains("reiniciado")) Color.Green else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(score: Int, onRestart: () -> Unit) {
    // Aquí implementaremos la lógica de las estrellas y mensajes en la Fase 3

    val message = when (score) {
        in 0..5 -> "Debes estudiar más"
        in 6..10 -> "Podría estar mejor"
        in 11..15 -> "Vamos, tú puedes, haz un mejor intento"
        in 16..20 -> "Muy bien hecho"
        else -> "¡Excelente!" // 21 a 25
    }

    val stars = when (score) {
        in 0..5 -> "★"
        in 6..10 -> "★★"
        in 11..15 -> "★★★"
        in 16..20 -> "★★★★"
        else -> "★★★★★"
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Resultados") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("¡Quiz Terminado!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stars, style = MaterialTheme.typography.displayLarge, color = Color(0xFFFCC007)) // Color amarillo para estrellas
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Tu puntaje: $score / 25", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onRestart) {
                Text("Volver al Inicio")
            }
        }
    }
}

// ----------------------------------------------------------------------
// FASE 2.3: PANTALLA DEL QUIZ (Integración)
// ----------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quizViewModel: QuizViewModel,
    onQuizFinished: (Int) -> Unit,
    onBack: () -> Unit
) {
    // Usamos collectAsState para observar los datos del ViewModel
    val currentQuestion by quizViewModel.currentQuestion.collectAsState()
    val score by quizViewModel.score.collectAsState()
    val quizState by quizViewModel.quizState.collectAsState()
    val questionIndex by quizViewModel.currentQuestionIndex.collectAsState()

    // Estado local para el feedback visual (rojo/verde/shake)
    var selectedOptionId by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var shakeKey by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // 1. Manejo de fin del Quiz
    if (quizState == 1) {
        LaunchedEffect(Unit) {
            onQuizFinished(score)
        }
        return
    }

    // 2. Lógica de Respuesta y Feedback
    val handleAnswer: (String) -> Unit = { selectedText ->
        if (!showFeedback) {
            selectedOptionId = selectedText
            showFeedback = true

            val isCorrect = quizViewModel.checkAnswerAndAdvance(selectedText)

            if (!isCorrect) {
                // Feedback Táctil (Animación SHAKE) - Requisito del proyecto
                shakeKey++

                // Retraso para que el usuario vea el feedback Rojo y la sacudida
                coroutineScope.launch {
                    delay(1500)
                    showFeedback = false
                    selectedOptionId = null
                }
            } else {
                // Si es correcta, el ViewModel avanza después de 0.5s (delay interno del VM)
                // Retraso para que el usuario vea el feedback Verde
                coroutineScope.launch {
                    delay(500)
                    showFeedback = false
                    selectedOptionId = null
                }
            }
        }
    }

    // 3. Implementación de la Interfaz Táctil
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pregunta ${questionIndex + 1} de ${quizViewModel.quizQuestions.size}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { paddingValues ->
        if (currentQuestion != null) {
            QuizContent(
                question = currentQuestion!!,
                onOptionSelected = handleAnswer,
                selectedOptionId = selectedOptionId, // NUEVO
                showFeedback = showFeedback,         // NUEVO
                shakeKey = shakeKey,                 // NUEVO
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Preparando Quiz...", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

// Contenido principal de la pregunta
@Composable
fun QuizContent(
    question: Question,
    onOptionSelected: (String) -> Unit,
    selectedOptionId: String?, // Opción seleccionada (NUEVO)
    showFeedback: Boolean,     // Mostrar feedback (NUEVO)
    shakeKey: Int,             // Clave para sacudida (NUEVO)
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.shake(shakeKey), // Aplicamos el SHAKE al contenedor de la pregunta
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = question.enunciado,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // Opciones
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            question.opciones.forEach { option ->
                OptionButton(
                    text = option,
                    onClick = { onOptionSelected(option) },
                    isSelected = selectedOptionId == option, // NEW
                    isCorrect = option == question.respuesta_correcta, // NEW
                    showFeedback = showFeedback, // NEW
                )
            }
        }
    }
}

// Botón de Opción (CON LÓGICA DE FEEDBACK FINAL)
@Composable
fun OptionButton(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
) {
    val baseColor = MaterialTheme.colorScheme.primaryContainer

    // 1. Lógica de Color (Feedback)
    val buttonColor = if (showFeedback) {
        when {
            isSelected && isCorrect -> Color(0xFF8BC34A) // VERDE FUERTE
            isSelected && !isCorrect -> Color(0xFFF44336) // ROJO FUERTE
            else -> baseColor
        }
    } else {
        baseColor
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            // Deshabilita el click si el feedback se está mostrando
            .clickable(enabled = !showFeedback) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = buttonColor, // Aplicamos el color lógico al Surface
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black, // Usamos Negro para un mejor contraste en la opción
                textAlign = TextAlign.Center
            )
        }
    }
}