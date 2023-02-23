package jp.kawagh.kiando.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import jp.kawagh.kiando.Question

@Entity(tableName = "tags", indices = [Index(value = ["title"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
)

data class TagWithQuestions(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            QuestionTagCrossRef::class,
            parentColumn = "tag_id",
            entityColumn = "question_id",
        )
    )
    val questions: List<Question>
)