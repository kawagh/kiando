package com.example.kiando


data class Move(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val isPromote: Boolean = false
)

data class Question(val boardState: BoardState, val description: String, val answerMove: Move)

val sampleQuestion = Question(initialBoardState, "角道を開ける手は?", Move(6, 2, 6, 3))