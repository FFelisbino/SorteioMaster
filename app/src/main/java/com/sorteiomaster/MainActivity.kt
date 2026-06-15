package com.sorteiomaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.sorteiomaster.data.model.PassoSorteio
import com.sorteiomaster.ui.SorteioViewModel
import com.sorteiomaster.ui.screens.TelaHistorico
import com.sorteiomaster.ui.screens.TelaInicial
import com.sorteiomaster.ui.screens.TelaResultado
import com.sorteiomaster.ui.theme.BrancoGelo
import com.sorteiomaster.ui.theme.CinzaCard
import com.sorteiomaster.ui.theme.DouradoSoft
import com.sorteiomaster.ui.theme.Roxo300
import com.sorteiomaster.ui.theme.Roxo900
import com.sorteiomaster.ui.theme.SorteioMasterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SorteioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SorteioMasterTheme {
                SorteioApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteioApp(viewModel: SorteioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val historico by viewModel.historico.collectAsState()
    var abaAtual by remember { mutableIntStateOf(0) }

    // Se chegou no resultado, vai para aba sorteio automaticamente
    LaunchedEffect(uiState.passo) {
        if (uiState.passo == PassoSorteio.RESULTADO) abaAtual = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎉 Sorteio Master",
                        fontWeight = FontWeight.ExtraBold,
                        color = DouradoSoft
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CinzaCard
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = CinzaCard) {
                NavigationBarItem(
                    selected = abaAtual == 0,
                    onClick = { abaAtual = 0 },
                    icon = { Icon(Icons.Default.EmojiEvents, null) },
                    label = { Text("Sorteio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DouradoSoft,
                        selectedTextColor = DouradoSoft,
                        indicatorColor = Roxo900,
                        unselectedIconColor = BrancoGelo.copy(0.4f),
                        unselectedTextColor = BrancoGelo.copy(0.4f)
                    )
                )
                NavigationBarItem(
                    selected = abaAtual == 1,
                    onClick = { abaAtual = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (historico.isNotEmpty())
                                    Badge(containerColor = Roxo300) {
                                        Text("${historico.size}")
                                    }
                            }
                        ) {
                            Icon(Icons.Default.History, null)
                        }
                    },
                    label = { Text("Histórico") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Roxo300,
                        selectedTextColor = Roxo300,
                        indicatorColor = Roxo900,
                        unselectedIconColor = BrancoGelo.copy(0.4f),
                        unselectedTextColor = BrancoGelo.copy(0.4f)
                    )
                )
            }
        },
        containerColor = Roxo900
    ) { padding ->
        when (abaAtual) {
            0 -> {
                when (uiState.passo) {
                    PassoSorteio.RESULTADO -> TelaResultado(
                        state = uiState,
                        onNovoSorteio = viewModel::novoSorteio,
                        onVoltar = viewModel::voltarParaInicio,
                        modifier = Modifier.padding(padding)
                    )
                    else -> TelaInicial(
                        state = uiState,
                        onUrlChange = viewModel::onUrlChange,
                        onBuscar = viewModel::buscarComentarios,
                        onFiltroMencoes = viewModel::onFiltroMencoesChange,
                        onMencaoObrigatoria = viewModel::onMencaoObrigatoriaChange,
                        onPermitirRepetidos = viewModel::onPermitirRepetidosChange,
                        onReservas = viewModel::onQuantidadeReservasChange,
                        onSortear = viewModel::realizarSorteio,
                        onNovoSorteio = viewModel::novoSorteio,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
            1 -> TelaHistorico(
                historico = historico,
                onDeletar = viewModel::deletarHistorico,
                onLimparTudo = viewModel::limparHistorico,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
