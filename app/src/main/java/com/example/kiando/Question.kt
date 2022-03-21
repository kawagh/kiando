package com.example.kiando


data class Question(val boardState: BoardState, val description: String, val answerMove: Move)

val sampleQuestion = Question(
    boardState = initialBoardState, description = "角道を開ける手は?",
    answerMove = Move(Position(6, 2), Position(5, 2)),
)
