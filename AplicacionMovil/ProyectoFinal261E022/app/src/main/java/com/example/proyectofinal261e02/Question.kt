// Archivo: Question.kt
package com.example.proyectofinal261e02

data class Question(
    val id_pregunta: Int,
    val enunciado: String,
    val opciones: List<String>,
    val respuesta_correcta: String
)