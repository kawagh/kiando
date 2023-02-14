package jp.kawagh.kiando

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.kawagh.kiando.data.AppDatabase
import jp.kawagh.kiando.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val repository: Repository,
    @ApplicationContext private val context: Context,
) :
    ViewModel() {
    private val db = AppDatabase.getInstance(context)
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

    fun renameById(questionId: Int, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val question = db.questionDao().findById(questionId)
            db.questionDao().updateQuestion(
                question.copy(description = newTitle)
            )
        }

    }

    fun addSampleQuestions() {
        if (uiState.questions.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                sampleQuestions.map { it.copy(id = 0) }.forEach {
                    db.questionDao().insert(it)
                }
            }
        }
    }

    fun loadQuestionsFromAsset() {
        val csvQuestionStream = context.resources.openRawResource(R.raw.questions)
        val csvReader = csvReader { this.delimiter = ';' }
        val csvQuestionContents = csvReader.readAllWithHeader(csvQuestionStream)
        println(csvQuestionContents)
        csvQuestionContents.forEach {
            val id = it.getValue("id").toInt()
            val description: String = it.getValue("description")
            val answerMove: Move = Converters().toMove(it.getValue("answerMove"))
            val sfen: String = it.getValue("sfen")
            val komadaiSfen: String = it.getValue("komadaiSfen")
            val parsedQuestion = Question(id, description, answerMove, sfen, komadaiSfen)
            viewModelScope.launch(Dispatchers.IO) {
                db.questionDao().insert(parsedQuestion)
            }
        }
    }
}

data class QuestionsUiState(
    val questions: List<Question> = emptyList()
)