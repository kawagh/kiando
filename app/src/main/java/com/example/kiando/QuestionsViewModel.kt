package com.example.kiando

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getInstance(application)
    internal val questions: LiveData<List<Question>> = db.questionDao().getAll()

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionDao().deleteAll()
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionDao().deleteById(question.id)
        }
    }
}