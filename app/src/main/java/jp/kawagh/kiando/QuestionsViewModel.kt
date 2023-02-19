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
import jp.kawagh.kiando.models.QuestionTagCrossRef
import jp.kawagh.kiando.models.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val repository: Repository,
    private val db: AppDatabase,
    @ApplicationContext private val context: Context,
) :
    ViewModel() {
    var uiState: QuestionsUiState by mutableStateOf(QuestionsUiState())

    init {
        viewModelScope.launch() {
            db.questionDao().getQuestionsWithTags().collectLatest {
                uiState = uiState.copy(questionsWithTags = it)
            }
        }
        viewModelScope.launch {
            db.tagDao().getAll().collect {
                uiState = uiState.copy(tags = it)

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
            db.questionDao().updateQuestion(question.copy(isFavorite = !question.isFavorite))
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

    /**
     * Records with negative id are predefined.
     */
    fun addSampleQuestionsAndTags() {
        if (uiState.questionsWithTags.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                db.tagDao().insert(Tag(id = -1, title = "sample"))
                sampleQuestions.reversed().forEachIndexed { index, question ->
                    val questionId = -(index + 1)
                    db.questionDao().insert(question.copy(id = questionId))
                    db.questionTagCrossRefDao()
                        .insert(QuestionTagCrossRef(questionId = questionId, tagId = -1))
                }
            }
        }
    }

    fun add(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            db.tagDao().insert(tag)
        }
    }

    fun addCrossRef(question: Question, tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionTagCrossRefDao().insert(QuestionTagCrossRef(question.id, tag.id))
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
    val questionsWithTags: List<QuestionWithTags> = emptyList(),
    val tags: List<Tag> = emptyList()
)