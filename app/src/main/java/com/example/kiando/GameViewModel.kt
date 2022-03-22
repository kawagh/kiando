package com.example.kiando

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel

const val BOARD_SIZE = 9

class GameViewModel : ViewModel() {
    var boardState: SnapshotStateList<PanelState> =
        List(9 * 9) {
            PanelState.Empty(it / 9, it % 9)
        }.toMutableStateList()

    init {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val index = i * 9 + j
                boardState[index] = initialBoardState[i][j]
            }
        }
    }

    fun move(move: Move) {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        val toIndex = move.to.row * BOARD_SIZE + move.to.column
        boardState[toIndex] = boardState[fromIndex]
        boardState[fromIndex] = PanelState.Empty(move.from.row, move.from.column)
    }
}