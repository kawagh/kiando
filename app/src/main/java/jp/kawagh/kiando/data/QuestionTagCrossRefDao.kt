package jp.kawagh.kiando.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import jp.kawagh.kiando.models.QuestionTagCrossRef

@Dao
interface QuestionTagCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg questionTagCrossRef: QuestionTagCrossRef)
}