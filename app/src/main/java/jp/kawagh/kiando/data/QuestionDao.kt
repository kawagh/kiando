package jp.kawagh.kiando.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import jp.kawagh.kiando.models.Question
import jp.kawagh.kiando.models.QuestionWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAll(): Flow<List<Question>>

    @Transaction
    @Query("SELECT * FROM questions")
    fun getQuestionsWithTags(): Flow<List<QuestionWithTags>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    fun findById(questionId: Int): Question

    @Query("DELETE FROM questions WHERE id = :questionId")
    fun deleteById(questionId: Int)

    @Query("DELETE FROM questions")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: Question)

    @Update
    fun updateQuestion(question: Question)
}
