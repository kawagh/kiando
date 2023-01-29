package jp.kawagh.kiando.data

import jp.kawagh.kiando.Question
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImplRepository @Inject constructor(
    private val questionDao: QuestionDao
) : Repository {
    override val questions: Flow<List<Question>> = questionDao.getAll()
    override fun findById(questionId: Int): Question {
        return questionDao.findById(questionId)
    }

    override fun deleteById(questionId: Int) {
        return questionDao.deleteById(questionId)
    }

    override fun deleteAll() {
        questionDao.deleteAll()
    }

    override suspend fun insert(question: Question) {
        questionDao.insert(question)
    }

    override fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question)
    }
}