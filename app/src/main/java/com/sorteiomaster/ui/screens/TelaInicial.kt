package com.sorteiomaster.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorteiomaster.data.model.Comentario
import com.sorteiomaster.data.model.PassoSorteio
import com.sorteiomaster.data.model.SorteioUiState
import com.sorteiomaster.ui.theme.*

@Composable
fun TelaInicial(
    state: SorteioUiState,
    onUrlChange: (String) -> Unit,
    onBuscar: () -> Unit,
    onFiltroMencoes: (Int) -> Unit,
    onMencaoObrigatoria: (String) -> Unit,
    onPermitirRepetidos: (Boolean) -> Unit,
    onReservas: (Int) -> Unit,
    onSortear: () -> Unit,
    onNovoSorteio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Roxo900, Color(0xFF0D001A)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Text("🎉", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Sorteio Master",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DouradoSoft
            )
            Text(
                "Sorteios transparentes para Instagram",
                fontSize = 14.sp,
                color = BrancoGelo.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Card: URL do post ──────────────────────────────
            CardSection(titulo = "🔗 Link do Post") {
                OutlinedTextField(
                    value = state.urlPost,
                    onValueChange = onUrlChange,
                    placeholder = {
                        Text(
                            "https://www.instagram.com/p/...",
                            color = BrancoGelo.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
                    colors = textFieldColors(),
                    trailingIcon = {
                        if (state.urlPost.isNotBlank()) {
                            IconButton(onClick = { onUrlChange("") }) {
                                Icon(Icons.Default.Clear, "Limpar", tint = BrancoGelo.copy(0.5f))
                            }
                        } else {
                            IconButton(onClick = {
                                clipboard.getText()?.text?.let { onUrlChange(it) }
                            }) {
                                Icon(Icons.Default.ContentPaste, "Colar", tint = Roxo300)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Botão buscar
                Button(
                    onClick = onBuscar,
                    enabled = !state.carregando && state.urlPost.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Roxo500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.carregando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Buscando comentários...")
                    } else {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Buscar Comentários", fontWeight = FontWeight.Bold)
                    }
                }

                // Erro
                AnimatedVisibility(state.erro != null) {
                    state.erro?.let { erro ->
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = Erro.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    null,
                                    tint = Erro,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(erro, color = Erro, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ── Resultado da busca ─────────────────────────────
            AnimatedVisibility(
                visible = state.passo == PassoSorteio.COMENTARIOS_CARREGADOS,
                enter = fadeIn() + slideInVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    // Estatísticas
                    CardSection(titulo = "📊 Comentários Encontrados") {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EstatCard("Total", state.comentarios.size.toString(), Roxo300)
                            EstatCard(
                                "Válidos",
                                state.comentariosFiltrados.size.toString(),
                                DouradoSoft
                            )
                            EstatCard(
                                "Únicos",
                                state.comentarios.distinctBy { it.username }.size.toString(),
                                VerdeClaro
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Filtros ────────────────────────────────
                    CardSection(titulo = "⚙️ Filtros do Sorteio") {

                        // Menção obrigatória
                        Text(
                            "@ obrigatória no comentário",
                            color = BrancoGelo.copy(0.7f),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.mencaoObrigatoria,
                            onValueChange = onMencaoObrigatoria,
                            placeholder = {
                                Text(
                                    "@usuario (opcional)",
                                    color = BrancoGelo.copy(0.35f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            prefix = { if (state.mencaoObrigatoria.isNotEmpty() && !state.mencaoObrigatoria.startsWith("@")) Text("@") }
                        )

                        Spacer(Modifier.height(16.dp))

                        // Mínimo de menções
                        Text(
                            "Mínimo de amigos marcados: ${state.filtroMencoes}",
                            color = BrancoGelo.copy(0.7f),
                            fontSize = 13.sp
                        )
                        Slider(
                            value = state.filtroMencoes.toFloat(),
                            onValueChange = { onFiltroMencoes(it.toInt()) },
                            valueRange = 0f..5f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = DouradoSoft,
                                activeTrackColor = DouradoSoft
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        // Permitir repetidos
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Permitir múltiplos comentários",
                                    color = BrancoGelo,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Mesmo usuário pode vencer com qualquer comentário",
                                    color = BrancoGelo.copy(0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = state.permitirRepetidos,
                                onCheckedChange = onPermitirRepetidos,
                                colors = SwitchDefaults.colors(checkedThumbColor = DouradoSoft)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Quantidade de reservas
                        Text(
                            "Vencedores reserva: ${state.quantidadeReservas}",
                            color = BrancoGelo.copy(0.7f),
                            fontSize = 13.sp
                        )
                        Slider(
                            value = state.quantidadeReservas.toFloat(),
                            onValueChange = { onReservas(it.toInt()) },
                            valueRange = 0f..5f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = VerdeClaro,
                                activeTrackColor = VerdeClaro
                            )
                        )

                        // Aviso se filtros removeram muito
                        if (state.comentarios.isNotEmpty() &&
                            state.comentariosFiltrados.size < state.comentarios.size * 0.1f &&
                            state.comentariosFiltrados.isNotEmpty()
                        ) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = DouradoSoft.copy(0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "⚠️ Os filtros reduziram muito os participantes. Verifique as configurações.",
                                    color = DouradoSoft,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botão sortear
                    Button(
                        onClick = onSortear,
                        enabled = state.comentariosFiltrados.isNotEmpty() && !state.sorteando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DouradoSoft),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.sorteando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Roxo900,
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Sorteando...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Roxo900
                            )
                        } else {
                            Text(
                                "🎲 Realizar Sorteio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Roxo900
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = onNovoSorteio,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Novo sorteio",
                            color = BrancoGelo.copy(0.5f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CardSection(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = CinzaCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                titulo,
                color = BrancoGelo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun EstatCard(label: String, valor: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valor,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = cor
        )
        Text(
            label,
            fontSize = 12.sp,
            color = BrancoGelo.copy(0.6f)
        )
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Roxo300,
    unfocusedBorderColor = BrancoGelo.copy(0.2f),
    focusedTextColor = BrancoGelo,
    unfocusedTextColor = BrancoGelo,
    cursorColor = Roxo300,
    focusedContainerColor = Roxo900.copy(0.5f),
    unfocusedContainerColor = Roxo900.copy(0.3f)
)
