package jp.kawagh.kiando

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.kawagh.kiando.data.ImplRepository
import jp.kawagh.kiando.data.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(repository: ImplRepository): Repository
}
