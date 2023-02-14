package jp.kawagh.kiando.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import jp.kawagh.kiando.Question

@Entity(
    primaryKeys = ["question_id", "tag_id"],
    tableName = "question_movie_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
        )
    ]
)
data class QuestionTagCrossRef(
    @ColumnInfo(name = "question_id")
    val questionId: Int,
    @ColumnInfo(name = "tag_id", index = true)
    val tagId: Int
)