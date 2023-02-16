package jp.kawagh.kiando.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import jp.kawagh.kiando.Converters
import jp.kawagh.kiando.Question
import jp.kawagh.kiando.models.QuestionTagCrossRef
import jp.kawagh.kiando.models.Tag

@Database(
    entities = [Question::class, Tag::class, QuestionTagCrossRef::class],
    autoMigrations = [AutoMigration(4, 5)],
    version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun tagDao(): TagDao
    abstract fun questionTagCrossRefDao(): QuestionTagCrossRefDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context, AppDatabase::class.java,
                    "database"
                )
                    .addMigrations(MIGRATION2to3, MIGRATION3to4)
                    .build()
            }
            return INSTANCE as AppDatabase
        }
    }
}

val MIGRATION2to3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE questions ADD COLUMN tag_id INTEGER"
        )
    }
}
val MIGRATION3to4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
    }
}
