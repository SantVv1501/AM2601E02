// Archivo: QuizViewModel.kt
package com.example.proyectofinal261e02

import com.example.proyectofinal261e02.FullQuestionBank
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class QuizViewModel : ViewModel() {

    // Esta lista contendrá SOLO las 25 preguntas aleatorias del examen actual
    // La hacemos privada para que solo el ViewModel la modifique
    private var _quizQuestions: List<Question> = emptyList()

    // Propiedad pública que la UI usa para saber cuántas preguntas hay (serán 25)
    val quizQuestions: List<Question>
        get() = _quizQuestions

    // --- ESTADOS OBSERVABLES ---
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _quizState = MutableStateFlow(0) // 0=En Curso, 1=Terminado
    val quizState: StateFlow<Int> = _quizState

    // El objeto observable de la pregunta actual
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion


    init {
        // Inicializa el quiz con 25 preguntas aleatorias al cargar el ViewModel
        resetQuiz()
    }

    /**
     * Resetea el quiz, baraja las 100 preguntas del banco y selecciona 25 aleatorias.
     */
    fun resetQuiz() {
        // Lógica de Randomización (¡La clave!):
        _quizQuestions = FullQuestionBank
            .shuffled() // 1. Baraja TODAS las 100 preguntas
            .take(25)    // 2. Toma SOLO las primeras 25 de la lista barajada

        // Resetea los estados para un nuevo examen:
        _currentQuestionIndex.value = 0
        _score.value = 0
        _quizState.value = 0 // En Curso

        // Establece la primera pregunta de la nueva lista de 25
        _currentQuestion.value = _quizQuestions.firstOrNull()
    }

    // --- LÓGICA DE RESPUESTA ---

    fun checkAnswerAndAdvance(selectedOption: String): Boolean {
        val question = _currentQuestion.value ?: return false
        val isCorrect = selectedOption == question.respuesta_correcta

        // 1. Actualiza el puntaje si es correcta
        if (isCorrect) {
            _score.value += 1
        }

        // 2. Ejecuta el avance después del delay visual
        viewModelScope.launch {

            // El tiempo de espera debe ser diferente:
            // 0.5s para la correcta (verde) y 1.5s para la incorrecta (rojo/shake)
            val delayTime = if (isCorrect) 500L else 1500L

            delay(delayTime)

            // Llama a la función que mueve el índice a la siguiente pregunta
            advanceQuestion()
        }

        return isCorrect // Devuelve el resultado de la corrección
    }

    fun advanceQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _quizQuestions.size) {
            _currentQuestionIndex.value = nextIndex
            _currentQuestion.value = _quizQuestions.getOrNull(nextIndex)
        } else {
            _quizState.value = 1 // Fin del quiz
        }
    }

    // Helper requerido por WelcomeScreen
    fun setQuizState(state: Int) {
        _quizState.value = state
    }
}