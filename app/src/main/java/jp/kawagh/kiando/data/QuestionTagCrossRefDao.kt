@file:Suppress("MaxLineLength")

package jp.kawagh.kiando.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.kawagh.kiando.models.QuestionTagCrossRef

@Dao
interface QuestionTagCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg questionTagCrossRef: QuestionTagCrossRef)

    @Query("DELETE FROM question_tag_cross_ref")
    fun deleteAll()

    @Delete
    fun deleteCrossRef(crossRef: QuestionTagCrossRef)

    @Query(
        "SELECT EXISTS (SELECT 1 FROM question_tag_cross_ref WHERE tag_id = :tagId AND question_id = :questionId) LIMIT 1"
    )
    fun hasCrossRef(tagId: Int, questionId: Int): Boolean
}
