package com.sorteiomaster.data.db

import androidx.room.*
import com.sorteiomaster.data.model.ListConverters
import com.sorteiomaster.data.model.SorteioHistorico
import kotlinx.coroutines.flow.Flow

@Dao
interface SorteioDao {
    @Query("SELECT * FROM historico_sorteios ORDER BY dataHora DESC")
    fun getHistorico(): Flow<List<SorteioHistorico>>

    @Query("SELECT * FROM historico_sorteios ORDER BY dataHora DESC LIMIT 1")
    suspend fun getUltimo(): SorteioHistorico?

    @Insert
    suspend fun inserir(sorteio: SorteioHistorico): Long

    @Delete
    suspend fun deletar(sorteio: SorteioHistorico)

    @Query("DELETE FROM historico_sorteios WHERE id = :id")
    suspend fun deletarPorId(id: Long)

    @Query("DELETE FROM historico_sorteios")
    suspend fun limparTudo()

    @Query("SELECT COUNT(*) FROM historico_sorteios")
    suspend fun total(): Int
}

@Database(
    entities = [SorteioHistorico::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListConverters::class)
abstract class SorteioDatabase : RoomDatabase() {
    abstract fun sorteioDao(): SorteioDao

    companion object {
        @Volatile private var INSTANCE: SorteioDatabase? = null

        fun getInstance(context: android.content.Context): SorteioDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SorteioDatabase::class.java,
                    "sorteio_master.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
