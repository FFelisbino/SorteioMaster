package com.sorteiomaster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta: roxo profundo + dourado + branco gelo
// Inspirado em sorteios / confetes / celebração
val Roxo900    = Color(0xFF1A0533)
val Roxo700    = Color(0xFF3D0D6B)
val Roxo500    = Color(0xFF7B2FBE)
val Roxo300    = Color(0xFFBB86FC)
val Dourado    = Color(0xFFFFD700)
val DouradoSoft= Color(0xFFFFC947)
val BrancoGelo = Color(0xFFF5F0FF)
val CinzaCard  = Color(0xFF2A1040)
val Verde      = Color(0xFF4CAF50)
val VerdeClaro = Color(0xFF81C784)
val Erro       = Color(0xFFCF6679)

private val DarkColors = darkColorScheme(
    primary         = Roxo300,
    onPrimary       = Roxo900,
    primaryContainer= Roxo700,
    onPrimaryContainer = BrancoGelo,
    secondary       = DouradoSoft,
    onSecondary     = Roxo900,
    background      = Roxo900,
    onBackground    = BrancoGelo,
    surface         = CinzaCard,
    onSurface       = BrancoGelo,
    error           = Erro,
    onError         = Color.White
)

@Composable
fun SorteioMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
