package com.sorteiomaster.domain

import com.sorteiomaster.data.model.Comentario

object SorteioEngine {

    data class FiltroConfig(
        val mencaoObrigatoria: String = "",       // @ que DEVE aparecer no comentário
        val minimoMencoes: Int = 0,               // Mínimo de menções diferentes
        val permitirRepetidos: Boolean = true,    // Mesmo user pode vencer com vários comentários
        val excluirUsuarios: List<String> = emptyList() // Usuários excluídos do sorteio
    )

    data class ResultadoSorteio(
        val vencedor: Comentario,
        val reservas: List<Comentario>,
        val totalParticipantes: Int,
        val totalFiltrado: Int
    )

    /**
     * Aplica os filtros e retorna a lista válida para sorteio
     */
    fun filtrar(comentarios: List<Comentario>, config: FiltroConfig): List<Comentario> {
        var lista = comentarios

        // Filtro: menção obrigatória
        if (config.mencaoObrigatoria.isNotBlank()) {
            val alvo = config.mencaoObrigatoria.lowercase().removePrefix("@")
            lista = lista.filter { c ->
                c.mencoes.any { it.lowercase() == alvo }
            }
        }

        // Filtro: mínimo de menções
        if (config.minimoMencoes > 0) {
            lista = lista.filter { c -> c.mencoes.size >= config.minimoMencoes }
        }

        // Filtro: excluir usuários específicos
        if (config.excluirUsuarios.isNotEmpty()) {
            val excluidos = config.excluirUsuarios.map { it.lowercase().removePrefix("@") }
            lista = lista.filter { c -> c.username.lowercase() !in excluidos }
        }

        // Filtro: sem repetidos (pega apenas 1 comentário por username)
        if (!config.permitirRepetidos) {
            lista = lista.distinctBy { it.username.lowercase() }
        }

        return lista
    }

    /**
     * Realiza o sorteio com aleatoriedade verdadeira (SecureRandom)
     */
    fun sortear(
        comentariosFiltrados: List<Comentario>,
        quantidadeReservas: Int = 2
    ): ResultadoSorteio? {
        if (comentariosFiltrados.isEmpty()) return null

        val rng = java.security.SecureRandom()
        val pool = comentariosFiltrados.toMutableList()

        // Embaralha com Fisher-Yates usando SecureRandom
        for (i in pool.size - 1 downTo 1) {
            val j = rng.nextInt(i + 1)
            val temp = pool[i]
            pool[i] = pool[j]
            pool[j] = temp
        }

        val vencedor = pool.first()
        val reservas = pool.drop(1).take(quantidadeReservas)

        return ResultadoSorteio(
            vencedor = vencedor,
            reservas = reservas,
            totalParticipantes = comentariosFiltrados.size,
            totalFiltrado = comentariosFiltrados.size
        )
    }

    /**
     * Estatísticas do post
     */
    fun estatisticas(comentarios: List<Comentario>): Map<String, Any> {
        val porUsuario = comentarios.groupBy { it.username.lowercase() }
        val maisAtivos = porUsuario.entries
            .sortedByDescending { it.value.size }
            .take(5)
            .map { it.key to it.value.size }

        val comMencoes = comentarios.count { it.mencoes.isNotEmpty() }
        val mediasMencoes = if (comentarios.isNotEmpty())
            comentarios.sumOf { it.mencoes.size }.toFloat() / comentarios.size
        else 0f

        return mapOf(
            "total" to comentarios.size,
            "usuarios_unicos" to porUsuario.size,
            "com_mencoes" to comMencoes,
            "media_mencoes" to mediasMencoes,
            "mais_ativos" to maisAtivos
        )
    }
}
