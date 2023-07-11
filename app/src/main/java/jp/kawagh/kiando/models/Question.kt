package jp.kawagh.kiando.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import jp.kawagh.kiando.SFENConverter

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    @ColumnInfo(name = "answer_move")
    val answerMove: Move,
    val sfen: String,
    @ColumnInfo(name = "komadai_sfen")
    val komadaiSfen: String,
    @ColumnInfo("is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,
    @ColumnInfo("answer_description", defaultValue = "")
    val answerDescription: String = ""
) {
    val boardState
        get() = SFENConverter().convertFrom(sfen)
    val myKomadai
        get() = SFENConverter().convertKomadaiFrom(komadaiSfen).first
    val enemyKomadai
        get() = SFENConverter().convertKomadaiFrom(komadaiSfen).second
}

data class QuestionWithTags(
    @Embedded val question: Question,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            QuestionTagCrossRef::class,
            parentColumn = "question_id",
            entityColumn = "tag_id",
        )
    )
    val tags: List<Tag>
)

const val INITIAL_SFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL"
const val SAMPLE3_SFEN = "lnsgkgsnl/1r5b1/pppppp1pp/6p2/9/2P6/PP1PPPPPP/1B5R1/LNSGKGSNL b - 1"

val sampleQuestionWithLongDescription = Question(
    id = -3,
    description = "LongDescriptionLongDescriptionLongDescription" +
        "LongDescriptionLongDescriptionLongDescriptionLongDescription",
    answerMove = Move(Position(6, 2), Position(5, 2)),
    sfen = INITIAL_SFEN,
    komadaiSfen = ""
)
val sampleQuestion = Question(
    id = -3,
    description = "角道を開ける手は?",
    answerMove = Move(Position(6, 2), Position(5, 2)),
    sfen = INITIAL_SFEN,
    komadaiSfen = ""
)
val sampleQuestion2 = Question(
    id = -2,
    description = "飛車先を突く手は?",
    answerMove = Move(Position(6, 7), Position(5, 7)),
    sfen = INITIAL_SFEN,
    komadaiSfen = ""
)

val sampleQuestion3 =
    Question(
        id = -1,
        description = "角交換を防ぐ手は?",
        answerMove = Move(
            Position(6, 3),
            Position(5, 3)
        ),
        sfen = SAMPLE3_SFEN,
        komadaiSfen = ""
    )
val sampleQuestions: List<Question> = listOf(
    sampleQuestion,
    sampleQuestion2,
    sampleQuestion3,
)
