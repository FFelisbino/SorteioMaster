package com.sorteiomaster.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sorteiomaster.data.db.SorteioDatabase
import com.sorteiomaster.data.model.Comentario
import com.sorteiomaster.data.model.PassoSorteio
import com.sorteiomaster.data.model.SorteioHistorico
import com.sorteiomaster.data.model.SorteioUiState
import com.sorteiomaster.domain.InstagramScraper
import com.sorteiomaster.domain.SorteioEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SorteioViewModel(app: Application) : AndroidViewModel(app) {

    private val scraper = InstagramScraper()
    private val db = SorteioDatabase.getInstance(app)
    private val dao = db.sorteioDao()

    private val _uiState = MutableStateFlow(SorteioUiState())
    val uiState: StateFlow<SorteioUiState> = _uiState.asStateFlow()

    val historico = dao.getHistorico()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Entrada de URL ──────────────────────────────────────────
    fun onUrlChange(url: String) {
        _uiState.update { it.copy(urlPost = url, erro = null) }
    }

    fun onFiltroMencoesChange(valor: Int) {
        _uiState.update { it.copy(filtroMencoes = valor) }
        aplicarFiltros()
    }

    fun onMencaoObrigatoriaChange(mencao: String) {
        _uiState.update { it.copy(mencaoObrigatoria = mencao) }
        aplicarFiltros()
    }

    fun onPermitirRepetidosChange(valor: Boolean) {
        _uiState.update { it.copy(permitirRepetidos = valor) }
        aplicarFiltros()
    }

    fun onQuantidadeReservasChange(valor: Int) {
        _uiState.update { it.copy(quantidadeReservas = valor) }
    }

    // ── Buscar comentários ──────────────────────────────────────
    fun buscarComentarios() {
        val url = _uiState.value.urlPost.trim()
        if (url.isBlank()) {
            _uiState.update { it.copy(erro = "Cole o link do post do Instagram") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }

            when (val resultado = scraper.buscarComentarios(url)) {
                is InstagramScraper.Resultado.Sucesso -> {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            comentarios = resultado.comentarios,
                            comentariosFiltrados = resultado.comentarios,
                            passo = PassoSorteio.COMENTARIOS_CARREGADOS,
                            erro = null
                        )
                    }
                    aplicarFiltros()
                }
                is InstagramScraper.Resultado.Erro -> {
                    _uiState.update {
                        it.copy(carregando = false, erro = resultado.mensagem)
                    }
                }
            }
        }
    }

    // ── Aplicar filtros ─────────────────────────────────────────
    private fun aplicarFiltros() {
        val state = _uiState.value
        val config = SorteioEngine.FiltroConfig(
            mencaoObrigatoria = state.mencaoObrigatoria,
            minimoMencoes = state.filtroMencoes,
            permitirRepetidos = state.permitirRepetidos
        )
        val filtrados = SorteioEngine.filtrar(state.comentarios, config)
        _uiState.update { it.copy(comentariosFiltrados = filtrados) }
    }

    // ── Realizar sorteio ────────────────────────────────────────
    fun realizarSorteio() {
        val state = _uiState.value
        if (state.comentariosFiltrados.isEmpty()) {
            _uiState.update { it.copy(erro = "Nenhum comentário válido para sortear com os filtros atuais") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(sorteando = true, erro = null) }

            // Animação de suspense
            delay(2000)

            val resultado = SorteioEngine.sortear(
                state.comentariosFiltrados,
                state.quantidadeReservas
            )

            if (resultado == null) {
                _uiState.update {
                    it.copy(sorteando = false, erro = "Não foi possível realizar o sorteio")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    sorteando = false,
                    vencedor = resultado.vencedor,
                    reservas = resultado.reservas,
                    passo = PassoSorteio.RESULTADO
                )
            }

            // Salvar no histórico
            salvarHistorico(state, resultado.vencedor, resultado.reservas)
        }
    }

    private suspend fun salvarHistorico(
        state: SorteioUiState,
        vencedor: Comentario,
        reservas: List<Comentario>
    ) {
        dao.inserir(
            SorteioHistorico(
                urlPost = state.urlPost,
                totalComentarios = state.comentarios.size,
                filtroMencoes = state.filtroMencoes,
                mencaoObrigatoria = state.mencaoObrigatoria,
                vencedor = vencedor.username,
                textoVencedor = vencedor.texto,
                reservas = reservas.map { it.username }
            )
        )
    }

    // ── Navegação ───────────────────────────────────────────────
    fun voltarParaInicio() {
        _uiState.update {
            SorteioUiState(urlPost = it.urlPost)
        }
    }

    fun novoSorteio() {
        _uiState.update { SorteioUiState() }
    }

    fun deletarHistorico(id: Long) {
        viewModelScope.launch { dao.deletarPorId(id) }
    }

    fun limparHistorico() {
        viewModelScope.launch { dao.limparTudo() }
    }
}
