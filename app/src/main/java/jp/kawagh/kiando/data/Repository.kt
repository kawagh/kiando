package jp.kawagh.kiando.data

import jp.kawagh.kiando.Question
import jp.kawagh.kiando.sampleQuestions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface Repository {
    val questions: Flow<List<Question>>
    fun findById(questionId: Int): Question
    fun deleteById(questionId: Int)
    fun deleteAll()
    suspend fun insert(question: Question)
    fun updateQuestion(question: Question)
}

class FakeRepository @Inject constructor() : Repository {
    override val questions: Flow<List<Question>> = flowOf(sampleQuestions)
    override fun findById(questionId: Int): Question {
        throw NotImplementedError()
    }

    override fun deleteById(questionId: Int) {
        throw NotImplementedError()
    }

    override fun deleteAll() {
        throw NotImplementedError()
    }

    override suspend fun insert(question: Question) {
        throw NotImplementedError()
    }

    override fun updateQuestion(question: Question) {
        throw NotImplementedError()
    }
}