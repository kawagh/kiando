package jp.kawagh.kiando.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import jp.kawagh.kiando.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAll(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag)

    @Query("DELETE FROM tags")
    fun deleteAll()

    @Query("DELETE FROM tags WHERE id = :tagId")
    fun deleteById(tagId: Int)

    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun findById(tagId: Int): Tag

    @Update
    fun update(tag: Tag)
}