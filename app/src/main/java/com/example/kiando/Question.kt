package com.example.kiando

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.lang.StringBuilder

class Converters {
    // encode Move -> fromRow_fromCol_toRow_toCol_isPromote
    @TypeConverter
    fun fromMove(move: Move): String = StringBuilder().apply {
        append(move.from.row)
        append('_')
        append(move.from.column)
        append('_')
        append(move.to.row)
        append('_')
        append(move.to.column)
        append('_')
        if (move.isPromote) append('1') else append('0')
    }.toString().trimStart().trimEnd()

    @TypeConverter
    fun toMove(s: String): Move {
        val tokens = s.split("_").map {
            it.toInt()
        }
        return Move(
            Position(tokens[0], tokens[1]),
            Position(tokens[2], tokens[3]),
            tokens[4] == 1
        )
    }

}


@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val answerMove: Move,
    val sfen: String,
) {
    val boardState
        get() = SFENConverter().convertFrom(sfen)

}

val initialSFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL"
val sample3_SFEN = "lnsgkgsnl/1+r5b1/pppppp1pp/6p2/9/2P6/PP1PPPPPP/1B5R1/LNSGKGSNL b - 1"

val sampleQuestion = Question(
    id = 0,
    description = "角道を開ける手は?",
    answerMove = Move(Position(6, 2), Position(5, 2)),
    sfen = initialSFEN,
)
val sampleQuestion2 = Question(
    id = 1,
    description = "飛車先を突く手は?",
    answerMove = Move(Position(6, 7), Position(5, 7)),
    sfen = initialSFEN,
)

val sampleQuestion3 =
    Question(
        id = 2,
        description = "角交換を防ぐ手は?",
        answerMove = Move(
            Position(6, 3),
            Position(5, 3)
        ),
        sfen = sample3_SFEN,
    )
val sampleQuestions: List<Question> = listOf(
    sampleQuestion,
    sampleQuestion2,
    sampleQuestion3,
)
