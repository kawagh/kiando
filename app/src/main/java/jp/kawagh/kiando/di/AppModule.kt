package jp.kawagh.kiando.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.kawagh.kiando.data.AppDatabase
import jp.kawagh.kiando.data.MIGRATION2to3
import jp.kawagh.kiando.data.MIGRATION3to4
import jp.kawagh.kiando.data.MIGRATION6to7
import jp.kawagh.kiando.data.QuestionDao
import jp.kawagh.kiando.data.QuestionTagCrossRefDao
import jp.kawagh.kiando.data.TagDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao = database.questionDao()

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideQuestionTagCrossRefDao(database: AppDatabase): QuestionTagCrossRefDao =
        database.questionTagCrossRefDao()

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "database")
            .addMigrations(MIGRATION2to3, MIGRATION3to4, MIGRATION6to7)
            .fallbackToDestructiveMigrationFrom(5)
            .build()
    }
}