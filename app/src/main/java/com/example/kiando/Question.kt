package com.example.kiando


data class Question(
    val boardState: BoardStateFlatten,
    val description: String,
    val answerMove: Move
)

val sampleQuestion = Question(
    boardState = initialBoardState.flatten(), description = "角道を開ける手は?",
    answerMove = Move(Position(6, 2), Position(5, 2)),
)
val sampleQuestion2 = Question(
    boardState = initialBoardState.flatten(), description = "飛車先を突く手は?",
    answerMove = Move(Position(6, 7), Position(5, 7)),
)
val sample3_SFEN = "lnsgkgsnl/1+r5b1/pppppp1pp/6p2/9/2P6/PP1PPPPPP/1B5R1/LNSGKGSNL b - 1"
val sampleQuestion3 =
    Question(
        SFENConverter().convertFrom(sample3_SFEN), "角交換を防ぐ手は?", Move(
            Position(6, 3),
            Position(5, 3)
        )
    )
val sampleQuestions: List<Question> = listOf(
    sampleQuestion,
    sampleQuestion2,
    sampleQuestion3,
)
