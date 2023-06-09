package jp.kawagh.kiando.data

import jp.kawagh.kiando.models.Question
import jp.kawagh.kiando.models.QuestionTagCrossRef
import jp.kawagh.kiando.models.QuestionWithTags
import jp.kawagh.kiando.models.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImplRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val tagDao: TagDao,
    private val crossRefDao: QuestionTagCrossRefDao,
) : Repository {
    override val questions: Flow<List<Question>> = questionDao.getAll()
    override val questionsWithTags: Flow<List<QuestionWithTags>> =
        questionDao.getQuestionsWithTags()
    override val tags: Flow<List<Tag>> = tagDao.getAll()

    override fun findQuestionById(questionId: Int): Question {
        return questionDao.findById(questionId)
    }

    override fun findTagById(tagId: Int): Tag {
        return tagDao.findById(tagId)
    }

    override fun deleteQuestionById(questionId: Int) {
        questionDao.deleteById(questionId)
    }

    override fun deleteTagById(tagId: Int) {
        tagDao.deleteById(tagId)
    }

    override fun deleteAllQuestions() {
        questionDao.deleteAll()
    }

    override fun deleteAllTags() {
        tagDao.deleteAll()
    }

    override fun deleteAllCrossRef() {
        crossRefDao.deleteAll()
    }

    override suspend fun add(question: Question) {
        questionDao.insert(question)
    }

    override suspend fun add(tag: Tag) {
        tagDao.insert(tag)
    }

    override suspend fun add(crossRef: QuestionTagCrossRef) {
        crossRefDao.insert(crossRef)
    }

    override suspend fun toggle(crossRef: QuestionTagCrossRef) {
        if (crossRefDao.hasCrossRef(crossRef.tagId, crossRef.questionId)) {
            crossRefDao.deleteCrossRef(crossRef)
        } else {
            crossRefDao.insert(crossRef)
        }
    }

    override fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question)
    }

    override fun updateTag(tag: Tag) {
        tagDao.update(tag)
    }
}
