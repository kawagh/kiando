package jp.kawagh.kiando

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.kawagh.kiando.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionsViewModel @Inject constructor(private val repository: Repository) :
    ViewModel() {
    var uiState: QuestionsUiState by mutableStateOf(QuestionsUiState())

    init {
        viewModelScope.launch() {
            repository.questions.collectLatest {
                uiState = uiState.copy(questions = it)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun deleteById(questionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(questionId)
        }
    }

    fun toggleQuestionFavorite(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            if (question.tag_id == null) {
                repository.updateQuestion(question.copy(tag_id = 1))
            } else {
                repository.updateQuestion(question.copy(tag_id = null))
            }
        }
    }
}

data class QuestionsUiState(
    val questions: List<Question> = emptyList()
)