import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(digit: Score)

    @Query("SELECT * FROM scores ORDER BY value DESC")
    fun getAllScores(): Flow<List<Score>>

    @Delete
    suspend fun deleteScore(digit: Score)
}