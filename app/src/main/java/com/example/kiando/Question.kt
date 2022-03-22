package com.example.kiando


data class Question(val boardState: BoardState, val description: String, val answerMove: Move)

val sampleQuestion = Question(
    boardState = initialBoardState, description = "角道を開ける手は?",
    answerMove = Move(Position(6, 2), Position(5, 2)),
)
val sampleQuestion2 = Question(
    boardState = initialBoardState, description = "飛車先を突く手は?",
    answerMove = Move(Position(6, 7), Position(5, 7)),
)
val sampleQuestions: List<Question> = listOf(
    sampleQuestion,
    sampleQuestion2,
)
