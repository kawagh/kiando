package jp.kawagh.kiando.data

import jp.kawagh.kiando.Question
import jp.kawagh.kiando.QuestionWithTags
import jp.kawagh.kiando.models.QuestionTagCrossRef
import jp.kawagh.kiando.models.Tag
import jp.kawagh.kiando.sampleQuestions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface Repository {
    val questions: Flow<List<Question>>
    val questionsWithTags: Flow<List<QuestionWithTags>>
    val tags: Flow<List<Tag>>
    fun findQuestionById(questionId: Int): Question
    fun findTagById(tagId: Int): Tag
    fun deleteQuestionById(questionId: Int)
    fun deleteTagById(tagId: Int)
    fun deleteAllQuestions()
    fun deleteAllTags()
    fun deleteAllCrossRef()
    suspend fun add(question: Question)
    suspend fun add(tag: Tag)
    suspend fun add(crossRef: QuestionTagCrossRef)
    suspend fun toggle(crossRef: QuestionTagCrossRef)
    fun updateQuestion(question: Question)
    fun updateTag(tag: Tag)
}

class FakeRepository @Inject constructor() : Repository {
    override val questions: Flow<List<Question>> = flowOf(sampleQuestions)
    override val questionsWithTags: Flow<List<QuestionWithTags>>
        get() = TODO("Not yet implemented")
    override val tags: Flow<List<Tag>>
        get() = TODO("Not yet implemented")

    override fun findQuestionById(questionId: Int): Question {
        throw NotImplementedError()
    }

    override fun findTagById(tagId: Int): Tag {
        TODO("Not yet implemented")
    }

    override fun deleteQuestionById(questionId: Int) {
        throw NotImplementedError()
    }

    override fun deleteTagById(tagId: Int) {
        TODO("Not yet implemented")
    }

    override fun deleteAllQuestions() {
        TODO("Not yet implemented")
    }

    override fun deleteAllTags() {
        TODO("Not yet implemented")
    }

    override fun deleteAllCrossRef() {
        TODO("Not yet implemented")
    }


    override suspend fun add(question: Question) {
        TODO("Not yet implemented")
    }

    override suspend fun add(tag: Tag) {
        TODO("Not yet implemented")
    }

    override suspend fun add(crossRef: QuestionTagCrossRef) {
        TODO("Not yet implemented")
    }

    override suspend fun toggle(crossRef: QuestionTagCrossRef) {
        TODO("Not yet implemented")
    }


    override fun updateQuestion(question: Question) {
        throw NotImplementedError()
    }

    override fun updateTag(tag: Tag) {
        TODO("Not yet implemented")
    }
}