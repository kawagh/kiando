package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kawagh.kiando.BOARD_SIZE
import jp.kawagh.kiando.models.PanelState
import jp.kawagh.kiando.models.PieceKind
import jp.kawagh.kiando.models.Position
import jp.kawagh.kiando.models.sampleQuestions
import jp.kawagh.kiando.ui.theme.BoardColor
import jp.kawagh.kiando.ui.theme.BoardColorUnfocused
import jp.kawagh.kiando.ui.theme.KiandoM3Theme

const val alongBoardSpace = 25

@Composable
fun Board(
    boardState: SnapshotStateList<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
) {
    val dotSize = 8
    val panelSize = 40
    val dpOffset = DpOffset(x = (alongBoardSpace / 2).dp, y = (-alongBoardSpace / 2).dp)
    val fontSize = 12.sp
    val numbers = "一二三四五六七八九"
    Box(
        Modifier
            .size((panelSize * BOARD_SIZE + alongBoardSpace).dp)
            .background(BoardColorUnfocused)
            .offset(
                y = alongBoardSpace.dp,
            )
    ) {
        Box(
            modifier = Modifier.offset(
                x = (alongBoardSpace / 2).dp,
                y = -(alongBoardSpace / 2).dp
            )
        ) {
            Column {
                repeat(BOARD_SIZE) { rowIndex ->
                    BoardRow(
                        boardState.subList(
                            rowIndex * BOARD_SIZE,
                            rowIndex * BOARD_SIZE + BOARD_SIZE
                        ),
                        handlePanelClick,
                        shouldHighlight,
                        lastClickedPanelPos,
                        positionsToHighlight,
                        panelSize
                    )
                }
            }
        }
        Box(
            Modifier
                .size(dotSize.dp)
                .offset(x = (3 * panelSize - dotSize / 2).dp, y = (3 * panelSize - dotSize / 2).dp)
                .offset(dpOffset.x, dpOffset.y)
                .clip(CircleShape)
                .background(Color.Black)
        )
        Box(
            Modifier
                .size(dotSize.dp)
                .offset(x = (6 * panelSize - dotSize / 2).dp, y = (3 * panelSize - dotSize / 2).dp)
                .offset(dpOffset.x, dpOffset.y)
                .clip(CircleShape)
                .background(Color.Black)
        )
        Box(
            Modifier
                .size(dotSize.dp)
                .offset(x = (3 * panelSize - dotSize / 2).dp, y = (6 * panelSize - dotSize / 2).dp)
                .offset(dpOffset.x, dpOffset.y)
                .clip(CircleShape)
                .background(Color.Black)
        )
        Box(
            Modifier
                .size(dotSize.dp)
                .offset(x = (6 * panelSize - dotSize / 2).dp, y = (6 * panelSize - dotSize / 2).dp)
                .offset(dpOffset.x, dpOffset.y)
                .clip(CircleShape)
                .background(Color.Black)
        )
        // texts along board
        repeat(9) {
            Text(
                "${9 - it}",
                Modifier
                    .offset(x = ((it * panelSize) + 15).dp, y = (-alongBoardSpace / 2).dp)
                    .offset(dpOffset.x, dpOffset.y)
                    .offset(y = (-2).dp),
                fontSize = fontSize
            )
        }
        numbers.forEachIndexed { index: Int, c: Char ->
            Text(
                c.toString(),
                modifier = Modifier
                    .offset(
                        x = (BOARD_SIZE * panelSize).dp,
                        y = ((10 + index * panelSize).dp)
                    )
                    .offset(dpOffset.x, dpOffset.y),
                fontSize = fontSize
            )
        }
    }
}

@Preview
@Composable
fun BoardPreview() {
    KiandoM3Theme {
        Board(
            boardState = sampleQuestions.first().boardState.toMutableStateList(),
            handlePanelClick = {},
            shouldHighlight = false,
            lastClickedPanelPos = Position(0, 0),
            positionsToHighlight = emptyList()
        )
    }
}

@Composable
private fun BoardRow(
    boardRow: List<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
    panelSize: Int,
) = Row {
    repeat(BOARD_SIZE) { colIndex ->
        Panel(
            boardRow[colIndex],
            handlePanelClick,
            shouldHighlight,
            lastClickedPanelPos,
            positionsToHighlight,
            panelSize,
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
    panelSize: Int,
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
    } else {
        BoardColorUnfocused
    }
    Piece(
        text = text,
        onClick = { handlePanelClick(panelState) },
        isEnemy = panelState.isEnemy,
        isPromoted = panelState.isPromoted,
        modifier = Modifier
            .size(panelSize.dp)
            .background(backgroundColor)
            .border(BorderStroke(0.4.dp, Color.Black))
            .semantics { contentDescription = "${panelState.row}-${panelState.column}" }
    )
}
