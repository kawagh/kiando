package com.example.kiando

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAll(): LiveData<List<Question>>

    @Query("DELETE FROM questions")
    fun deleteAll(): Unit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: Question)
}