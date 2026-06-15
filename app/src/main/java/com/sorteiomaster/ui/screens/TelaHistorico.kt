package com.sorteiomaster.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorteiomaster.data.model.SorteioHistorico
import com.sorteiomaster.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TelaHistorico(
    historico: List<SorteioHistorico>,
    onDeletar: (Long) -> Unit,
    onLimparTudo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D001A), Roxo900)))
    ) {
        if (historico.isEmpty()) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📋", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Nenhum sorteio realizado ainda",
                    color = BrancoGelo.copy(0.5f),
                    fontSize = 16.sp
                )
                Text(
                    "Os resultados aparecerão aqui",
                    color = BrancoGelo.copy(0.3f),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${historico.size} sorteio${if (historico.size != 1) "s" else ""}",
                            color = BrancoGelo.copy(0.6f),
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { mostrarConfirmacao = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                null,
                                tint = Erro.copy(0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Limpar tudo", color = Erro.copy(0.7f), fontSize = 13.sp)
                        }
                    }
                }

                items(historico, key = { it.id }) { item ->
                    HistoricoCard(
                        item = item,
                        onDeletar = { onDeletar(item.id) }
                    )
                }
            }
        }
    }

    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacao = false },
            title = { Text("Limpar histórico?") },
            text = { Text("Todos os sorteios serão removidos permanentemente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLimparTudo()
                        mostrarConfirmacao = false
                    }
                ) {
                    Text("Limpar", color = Erro)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacao = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = CinzaCard
        )
    }
}

@Composable
fun HistoricoCard(item: SorteioHistorico, onDeletar: () -> Unit) {
    val dataFormatada = remember(item.dataHora) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            .format(Date(item.dataHora))
    }

    Surface(
        color = CinzaCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "@${item.vencedor}",
                            fontWeight = FontWeight.ExtraBold,
                            color = DouradoSoft,
                            fontSize = 17.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.textoVencedor.take(80) + if (item.textoVencedor.length > 80) "..." else "",
                        color = BrancoGelo.copy(0.6f),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDeletar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = BrancoGelo.copy(0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = BrancoGelo.copy(0.08f))
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip("${item.totalComentarios} comentários")
                if (item.filtroMencoes > 0) InfoChip("${item.filtroMencoes}+ menções")
                if (item.mencaoObrigatoria.isNotBlank()) InfoChip("@${item.mencaoObrigatoria}")
            }

            if (item.reservas.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Reservas: ${item.reservas.joinToString(", ") { "@$it" }}",
                    color = BrancoGelo.copy(0.45f),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                dataFormatada,
                color = BrancoGelo.copy(0.3f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun InfoChip(texto: String) {
    Surface(
        color = Roxo700.copy(0.5f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            texto,
            fontSize = 11.sp,
            color = Roxo300,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
