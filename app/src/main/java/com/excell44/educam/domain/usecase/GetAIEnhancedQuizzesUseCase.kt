package com.excell44.educam.domain.usecase

import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAIEnhancedQuizzesUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    operator fun invoke(): Flow<List<QuizEntity>> {
        return repository.getQuizzes()
            .map { quizzes -> quizzes.filter { it.isAIEnhanced } }
    }
}
