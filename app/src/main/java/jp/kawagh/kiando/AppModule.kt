package jp.kawagh.kiando

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.kawagh.kiando.data.AppDatabase
import jp.kawagh.kiando.data.MIGRATION_2_3
import jp.kawagh.kiando.data.QuestionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao = database.questionDao()

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "database")
            .addMigrations(MIGRATION_2_3)
            .build()
    }
}