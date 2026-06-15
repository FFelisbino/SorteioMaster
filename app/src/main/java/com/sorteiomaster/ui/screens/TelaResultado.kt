package com.sorteiomaster.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorteiomaster.data.model.Comentario
import com.sorteiomaster.data.model.SorteioUiState
import com.sorteiomaster.ui.theme.*

@Composable
fun TelaResultado(
    state: SorteioUiState,
    onNovoSorteio: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vencedor = state.vencedor ?: return

    // Animação de entrada do troféu
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D001A), Roxo900)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Troféu animado
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(DouradoSoft, Dourado.copy(0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 64.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Temos um vencedor!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DouradoSoft
            )
            Text(
                "${state.comentariosFiltrados.size} participantes • sorteio verificável",
                fontSize = 13.sp,
                color = BrancoGelo.copy(0.5f)
            )

            Spacer(Modifier.height(28.dp))

            // Card do vencedor
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CinzaCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, DouradoSoft)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Roxo500, DouradoSoft))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            vencedor.username.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "@${vencedor.username}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DouradoSoft
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = Roxo900.copy(0.6f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "\"${vencedor.texto.take(120)}${if (vencedor.texto.length > 120) "..." else ""}\"",
                            color = BrancoGelo.copy(0.85f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (vencedor.mencoes.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            vencedor.mencoes.take(4).forEach { mencao ->
                                Surface(
                                    color = Roxo500.copy(0.3f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.padding(horizontal = 3.dp)
                                ) {
                                    Text(
                                        "@$mencao",
                                        fontSize = 11.sp,
                                        color = Roxo300,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botão compartilhar
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val texto = buildString {
                        appendLine("🎉 Sorteio realizado pelo Sorteio Master!")
                        appendLine()
                        appendLine("🏆 Vencedor: @${vencedor.username}")
                        if (state.reservas.isNotEmpty()) {
                            appendLine()
                            appendLine("Reservas:")
                            state.reservas.forEachIndexed { i, r ->
                                appendLine("  ${i + 1}º - @${r.username}")
                            }
                        }
                        appendLine()
                        appendLine("📊 ${state.comentariosFiltrados.size} participantes válidos")
                        appendLine("🔗 ${state.urlPost}")
                    }
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, texto)
                    }
                    context.startActivity(
                        android.content.Intent.createChooser(intent, "Compartilhar resultado")
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Roxo500),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(Modifier.width(8.dp))
                Text("Compartilhar Resultado", fontWeight = FontWeight.Bold)
            }

            // Reservas
            if (state.reservas.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                CardSection(titulo = "📋 Vencedores Reserva") {
                    state.reservas.forEachIndexed { index, reserva ->
                        if (index > 0) Divider(color = BrancoGelo.copy(0.08f), modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Roxo700),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}º",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrancoGelo
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "@${reserva.username}",
                                    color = BrancoGelo,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    reserva.texto.take(60),
                                    color = BrancoGelo.copy(0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Ações
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVoltar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrancoGelo.copy(0.3f))
                ) {
                    Icon(Icons.Default.Refresh, null, tint = BrancoGelo)
                    Spacer(Modifier.width(6.dp))
                    Text("Novo filtro", color = BrancoGelo)
                }
                Button(
                    onClick = onNovoSorteio,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Novo sorteio", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
