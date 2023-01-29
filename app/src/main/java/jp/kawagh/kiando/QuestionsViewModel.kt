package jp.kawagh.kiando

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getInstance(application)
    var uiState: QuestionsUiState by mutableStateOf(QuestionsUiState())

    init {
        viewModelScope.launch() {
            db.questionDao().getAll().collectLatest {
                uiState = uiState.copy(questions = it)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionDao().deleteAll()
        }
    }

    fun deleteById(questionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionDao().deleteById(questionId)
        }
    }

    fun toggleQuestionFavorite(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            if (question.tag_id == null) {
                db.questionDao().updateQuestion(question.copy(tag_id = 1))
            } else {
                db.questionDao().updateQuestion(question.copy(tag_id = null))
            }
        }
    }
}

data class QuestionsUiState(
    val questions: List<Question> = emptyList()
)