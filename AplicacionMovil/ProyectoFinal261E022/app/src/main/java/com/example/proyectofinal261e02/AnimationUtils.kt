// Archivo: AnimationUtils.kt (Versión Final y Funcional)
package com.example.proyectofinal261e02

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect // Necesario para el ciclo de vida
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.withFrameNanos // Usaremos esto para el smooth effect

private const val SHAKE_STRENGTH_PX = 30f

fun Modifier.shake(shakeKey: Int): Modifier = composed {

    // Usamos Animatable para controlar el valor actual del desplazamiento
    val shakeOffset = remember { Animatable(0f) }

    // Disparamos la animación cada vez que shakeKey cambia (ej: de 0 a 1)
    LaunchedEffect(shakeKey) {
        if (shakeKey > 0) {
            // Animación de ida y vuelta para simular el rebote
            shakeOffset.animateTo(
                targetValue = 0f, // El destino final es siempre 0 (reposo)
                animationSpec = repeatable(
                    iterations = 6, // Número de sacudidas
                    animation = keyframes {
                        durationMillis = 200
                        0f at 0 with FastOutSlowInEasing
                        SHAKE_STRENGTH_PX at 50 with EaseOutCubic // Movimiento a la derecha (rebote suave)
                        -SHAKE_STRENGTH_PX at 150 with EaseOutCubic // Movimiento a la izquierda
                        0f at 300 // Vuelve al centro
                    },
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    // Aplicamos el desplazamiento en el eje X basado en el valor actual de la animación
    // Esto resuelve el error de "Unresolved reference" y usa el valor animado.
    this.offset(x = shakeOffset.value.dp)
}