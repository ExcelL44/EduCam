package com.excell44.educam.di

import com.excell44.educam.data.referral.BetaReferralRepositoryImpl
import com.excell44.educam.data.repository.QuizRepositoryImpl
import com.excell44.educam.domain.referral.BetaReferralRepository
import com.excell44.educam.domain.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module DI pour les repositories.
 * Bind les interfaces de domaine avec leurs implémentations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind l'implémentation BetaReferralRepositoryImpl à l'interface BetaReferralRepository.
     */
    @Binds
    @Singleton
    abstract fun bindBetaReferralRepository(
        impl: BetaReferralRepositoryImpl
    ): BetaReferralRepository

    /**
     * Bind l'implémentation QuizRepositoryImpl à l'interface QuizRepository.
     */
    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        impl: QuizRepositoryImpl
    ): QuizRepository
}
