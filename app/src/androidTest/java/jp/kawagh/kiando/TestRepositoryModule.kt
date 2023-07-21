package jp.kawagh.kiando

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import jp.kawagh.kiando.data.FakeRepository
import jp.kawagh.kiando.data.Repository
import jp.kawagh.kiando.di.RepositoryModule
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
interface TestRepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(fakeRepository: FakeRepository): Repository
}