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
import jp.kawagh.kiando.data.Repository
import jp.kawagh.kiando.models.QuestionTagCrossRef
import jp.kawagh.kiando.models.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val repository: Repository,
    @ApplicationContext private val context: Context,
) :
    ViewModel() {
    var uiState: QuestionsUiState by mutableStateOf(QuestionsUiState())

    init {
        viewModelScope.launch() {
            repository.questionsWithTags.collect {
                uiState = uiState.copy(questionsWithTags = it)
            }
        }
        viewModelScope.launch {
            repository.tags.collect {
                uiState = uiState.copy(tags = it)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllQuestions()
        }
    }

    fun deleteQuestionById(questionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQuestionById(questionId)
        }
    }
    fun deleteTagById(tagId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTagById(tagId)
        }
    }

    fun toggleQuestionFavorite(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuestion(question.copy(isFavorite = !question.isFavorite))
        }
    }

    fun renameById(questionId: Int, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val question = repository.findById(questionId)
            repository.updateQuestion(
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
                repository.add(Tag(id = -1, title = "sample"))
                sampleQuestions.reversed().forEachIndexed { index, question ->
                    val questionId = -(index + 1)
                    repository.add(question.copy(id = questionId))
                    repository.add(QuestionTagCrossRef(questionId = questionId, tagId = -1))
                }
            }
        }
    }

    fun add(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.add(tag)
        }
    }

    fun toggleCrossRef(question: Question, tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggle(crossRef = QuestionTagCrossRef(question.id, tag.id))
        }
    }

    fun loadDataFromAsset() {
        loadQuestionsFromAsset()
        loadTagsFromAsset()
        loadCrossRefFromAsset()
    }

    private fun loadQuestionsFromAsset() {
        val csvQuestionStream = context.resources.openRawResource(R.raw.questions)
        val csvReader = csvReader { this.delimiter = ';' }
        val csvQuestionContents = csvReader.readAllWithHeader(csvQuestionStream)
        println(csvQuestionContents)
        csvQuestionContents.forEach {
            val id = it.getValue("id").toInt()
            val description: String = it.getValue("description")
            val answerMove: Move = Converters().toMove(it.getValue("answer_move"))
            val sfen: String = it.getValue("sfen")
            val komadaiSfen: String = it.getValue("komadai_sfen")
            val isFavorite: Boolean = it.getValue("is_favorite").toBoolean()
            val parsedQuestion =
                Question(id, description, answerMove, sfen, komadaiSfen, isFavorite)
            viewModelScope.launch(Dispatchers.IO) {
                repository.add(parsedQuestion)
            }
        }
    }

    private fun loadTagsFromAsset() {
        val csvTagStream = context.resources.openRawResource(R.raw.tags)
        val csvReader = csvReader { this.delimiter = ';' }
        val csvTagContents = csvReader.readAllWithHeader(csvTagStream)
        csvTagContents.forEach {
            val id = it.getValue("id").toInt()
            val title = it.getValue("title")
            val parsedTag = Tag(id = id, title = title)
            viewModelScope.launch(Dispatchers.IO) {
                repository.add(parsedTag)
            }
        }
    }

    private fun loadCrossRefFromAsset() {
        val csvCrossRefStream = context.resources.openRawResource(R.raw.question_tag_cross_ref)
        val csvReader = csvReader { this.delimiter = ';' }
        val csvCrossRefContents = csvReader.readAllWithHeader(csvCrossRefStream)
        csvCrossRefContents.forEach {
            val questionId = it.getValue("question_id").toInt()
            val tagId = it.getValue("tag_id").toInt()
            val parsedCrossRef = QuestionTagCrossRef(questionId, tagId)
            viewModelScope.launch(Dispatchers.IO) {
                repository.add(parsedCrossRef)
            }
        }
    }
}

data class QuestionsUiState(
    val questionsWithTags: List<QuestionWithTags> = emptyList(),
    val tags: List<Tag> = emptyList()
)