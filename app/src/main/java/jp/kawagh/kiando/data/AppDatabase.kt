package jp.kawagh.kiando.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import jp.kawagh.kiando.Converters
import jp.kawagh.kiando.Question
import jp.kawagh.kiando.models.QuestionTagCrossRef
import jp.kawagh.kiando.models.Tag

@Database(
    entities = [Question::class, Tag::class, QuestionTagCrossRef::class],
    autoMigrations = [
        AutoMigration(4, 5),
        AutoMigration(7, 8, spec = AppDatabase.AutoMigration7to8::class),
    ],
    version = 8
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    @DeleteColumn(tableName = "questions", columnName = "tag_id")
    @RenameColumn(
        tableName = "questions",
        fromColumnName = "answerMove",
        toColumnName = "answer_move"
    )
    @RenameColumn(
        tableName = "questions",
        fromColumnName = "komadaiSfen",
        toColumnName = "komadai_sfen"
    )
    class AutoMigration7to8 : AutoMigrationSpec

    abstract fun questionDao(): QuestionDao
    abstract fun tagDao(): TagDao
    abstract fun questionTagCrossRefDao(): QuestionTagCrossRefDao
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
val MIGRATION6to7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """ DROP TABLE IF EXISTS question_movie_cross_ref """.trimIndent()
        )
    }
}
