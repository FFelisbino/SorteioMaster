package com.sorteiomaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Comentário de um post do Instagram
data class Comentario(
    val username: String,
    val texto: String,
    val mencoes: List<String> = emptyList(),
    val timestamp: String = ""
)

// Conversor para salvar lista no Room
class ListConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromComentarioList(value: List<Comentario>): String = Gson().toJson(value)

    @TypeConverter
    fun toComentarioList(value: String): List<Comentario> =
        Gson().fromJson(value, object : TypeToken<List<Comentario>>() {}.type)
}

// Entidade de histórico no banco
@Entity(tableName = "historico_sorteios")
@TypeConverters(ListConverters::class)
data class SorteioHistorico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val urlPost: String,
    val totalComentarios: Int,
    val filtroMencoes: Int,              // Número mínimo de menções exigidas
    val mencaoObrigatoria: String,        // @ específica obrigatória (ou vazio)
    val vencedor: String,
    val textoVencedor: String,
    val reservas: List<String>,          // Vencedores reserva
    val dataHora: Long = System.currentTimeMillis()
)

// Estado da UI
data class SorteioUiState(
    val urlPost: String = "",
    val comentarios: List<Comentario> = emptyList(),
    val comentariosFiltrados: List<Comentario> = emptyList(),
    val filtroMencoes: Int = 0,
    val mencaoObrigatoria: String = "",
    val permitirRepetidos: Boolean = true,
    val quantidadeReservas: Int = 2,
    val carregando: Boolean = false,
    val sorteando: Boolean = false,
    val erro: String? = null,
    val vencedor: Comentario? = null,
    val reservas: List<Comentario> = emptyList(),
    val passo: PassoSorteio = PassoSorteio.URL_ENTRADA
)

enum class PassoSorteio {
    URL_ENTRADA,
    COMENTARIOS_CARREGADOS,
    RESULTADO
}
