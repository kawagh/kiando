package jp.kawagh.kiando.data

import jp.kawagh.kiando.Question
import jp.kawagh.kiando.sampleQuestions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface Repository {
    val questions: Flow<List<Question>>
}

class FakeRepository @Inject constructor() : Repository {
    override val questions: Flow<List<Question>> = flowOf(sampleQuestions)
}