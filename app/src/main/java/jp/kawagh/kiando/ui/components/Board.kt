package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kawagh.kiando.BOARD_SIZE
import jp.kawagh.kiando.PanelState
import jp.kawagh.kiando.PieceKind
import jp.kawagh.kiando.Position
import jp.kawagh.kiando.ui.theme.BoardColor
import jp.kawagh.kiando.ui.theme.BoardColorUnfocused

@Composable
fun Board(
    boardState: SnapshotStateList<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
) {
    Column {
        repeat(BOARD_SIZE) { rowIndex ->
            BoardRow(
                boardState.subList(rowIndex * BOARD_SIZE, rowIndex * BOARD_SIZE + BOARD_SIZE),
                handlePanelClick,
                shouldHighlight,
                lastClickedPanelPos,
                positionsToHighlight
            )
        }
    }
}

@Composable
private fun BoardRow(
    boardRow: List<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
) = Row {
    repeat(BOARD_SIZE) { colIndex ->
        Panel(
            boardRow[colIndex],
            handlePanelClick,
            shouldHighlight,
            lastClickedPanelPos,
            positionsToHighlight
        )

    }
}

@Composable
private fun Panel(
    panelState: PanelState,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
) {
    val text = when (panelState.pieceKind) {
        PieceKind.EMPTY -> ""
        PieceKind.KING -> "王"
        PieceKind.ROOK -> if (panelState.isPromoted) "龍" else "飛"
        PieceKind.BISHOP -> if (panelState.isPromoted) "馬" else "角"
        PieceKind.GOLD -> "金"
        PieceKind.SILVER -> if (panelState.isPromoted) "全" else "銀"
        PieceKind.KNIGHT -> if (panelState.isPromoted) "圭" else "桂"
        PieceKind.LANCE -> if (panelState.isPromoted) "杏" else "香"
        PieceKind.PAWN -> if (panelState.isPromoted) "と" else "歩"
    }
    val backgroundColor = if (shouldHighlight) {
        when (Position(panelState.row, panelState.column)) {
            lastClickedPanelPos -> BoardColor
            in positionsToHighlight -> BoardColor
            else -> BoardColorUnfocused
        }
    } else BoardColorUnfocused


    when (panelState.pieceKind) {
        PieceKind.EMPTY -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor)
                    .clickable { handlePanelClick.invoke(panelState) }
                    .border(BorderStroke(0.1.dp, Color.Black))
            ) {
                Text(
                    text = text, fontSize = 17.sp,
                    color = if (panelState.isPromoted) Color.Red else Color.Black,
                    modifier = if (panelState.isEnemy) Modifier.rotate(180f) else Modifier,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        else -> {
            Piece(
                text = text,
                onClick = { handlePanelClick(panelState) },
                isEnemy = panelState.isEnemy,
                isPromoted = panelState.isPromoted,
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor)
                    .border(BorderStroke(0.1.dp, Color.Black))
            )
        }
    }
}
