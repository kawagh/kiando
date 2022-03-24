package com.example.kiando

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Ignore val boardState: BoardStateFlatten,
    val description: String,
    val answerMove: Move,
    val sfen: String,
)

val initialSFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL"
val sample3_SFEN = "lnsgkgsnl/1+r5b1/pppppp1pp/6p2/9/2P6/PP1PPPPPP/1B5R1/LNSGKGSNL b - 1"

val sampleQuestion = Question(
    boardState = initialBoardState.flatten(), description = "角道を開ける手は?",
    answerMove = Move(Position(6, 2), Position(5, 2)),
    sfen = initialSFEN,
)
val sampleQuestion2 = Question(
    boardState = initialBoardState.flatten(), description = "飛車先を突く手は?",
    answerMove = Move(Position(6, 7), Position(5, 7)),
    sfen = initialSFEN,
)

val sampleQuestion3 =
    Question(
        boardState = SFENConverter().convertFrom(sample3_SFEN),
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
