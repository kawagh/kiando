package com.example.kiando

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiando.ui.theme.BoardColor
import com.example.kiando.ui.theme.BoardColorUnfocused

@Preview
@Composable
fun MainScreen() {
    val question = sampleQuestion
    val context = LocalContext.current
    val moveInfo = remember {
        mutableStateListOf<Int>()
    }
    var clickedPanelPos by remember {
        mutableStateOf(Position(-1, -1))
    }

    var panelClickedOnce by remember {
        mutableStateOf(false)
    }
    // TODO list legal moves
    val legalMovePositions = remember {
        mutableStateListOf<Position>()
    }
    val handlePanelClick: (PanelState) -> Unit = {
        when (panelClickedOnce) {
            true -> {
                panelClickedOnce = !panelClickedOnce
                moveInfo.addAll(listOf(it.row, it.column))
                val move = Move(
                    Position(moveInfo[0], moveInfo[1]),
                    Position(moveInfo[2], moveInfo[3])
                )
                Toast.makeText(context, move.toString(), Toast.LENGTH_SHORT).show()
                // judge
                if (move == question.answerMove) {
                    Toast.makeText(context, "Correct", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Wrong", Toast.LENGTH_SHORT).show()
                }
                moveInfo.clear()
                legalMovePositions.clear()
            }
            false -> {
                panelClickedOnce = !panelClickedOnce
                Toast.makeText(context, "$it clicked", Toast.LENGTH_SHORT).show()
                moveInfo.addAll(listOf(it.row, it.column))
                clickedPanelPos = Position(it.row, it.column)
                legalMovePositions.addAll(listLegalMoves(it))
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Board(
            sampleQuestion.boardState,
            handlePanelClick,
            panelClickedOnce,
            clickedPanelPos,
            legalMovePositions
        )
        Text(text = question.description, fontSize = MaterialTheme.typography.h5.fontSize)
    }
}

@Composable
private fun Board(
    boardState: BoardState,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    clickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Column {
        repeat(9) { rowIndex ->
            BoardRow(
                boardState[rowIndex],
                handlePanelClick,
                panelClickedOnce,
                clickedPanelPos,
                legalMovePositions
            )
        }
    }
}

@Composable
private fun BoardRow(
    boardRow: List<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    clickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Row() {
        repeat(9) { colIndex ->
            Panel(
                boardRow[colIndex],
                handlePanelClick,
                panelClickedOnce,
                clickedPanelPos,
                legalMovePositions
            )

        }
    }
}

@Composable
private fun Panel(
    panelState: PanelState,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    clickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Button(
        onClick = { handlePanelClick(panelState) },
        modifier = Modifier
            .size(40.dp)
            .border(BorderStroke(0.1.dp, Color.Black)),
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = if (panelClickedOnce) {
                when (Position(panelState.row, panelState.column)) {
                    clickedPanelPos -> BoardColor
                    in legalMovePositions -> BoardColor
                    else -> BoardColorUnfocused
                }
            } else {
                BoardColor
            },
            contentColor = Color.Black,
        )
    ) {
        when (panelState) {
            is PanelState.Empty -> Text(text = "", fontSize = 15.sp)
            is PanelState.Piece -> {
                val text = when (panelState.pieceKind) {
                    PieceKind.KING -> "王"
                    PieceKind.ROOK -> if (panelState.isPromoted) "龍" else "飛"
                    PieceKind.BISHOP -> if (panelState.isPromoted) "馬" else "角"
                    PieceKind.GOLD -> "金"
                    PieceKind.SILVER -> if (panelState.isPromoted) "全" else "銀"
                    PieceKind.KNIGHT -> if (panelState.isPromoted) "圭" else "桂"
                    PieceKind.LANCE -> if (panelState.isPromoted) "杏" else "香"
                    PieceKind.PAWN -> if (panelState.isPromoted) "と" else "歩"
                }
                Text(
                    text = text, fontSize = 17.sp,
                    modifier = if (panelState.isEnemy) Modifier.rotate(180f) else Modifier,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}