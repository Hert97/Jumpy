import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(@PrimaryKey(autoGenerate = true)
                     var id: Long = 0 ,
                     var value: Int)
{
}