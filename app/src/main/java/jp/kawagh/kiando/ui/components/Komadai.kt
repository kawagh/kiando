package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.BOARD_SIZE
import jp.kawagh.kiando.models.ENEMY_KOMADAI_INDEX
import jp.kawagh.kiando.models.MY_KOMADAI_INDEX
import jp.kawagh.kiando.models.PieceKind
import jp.kawagh.kiando.models.Position
import jp.kawagh.kiando.ui.theme.BoardColor
import jp.kawagh.kiando.ui.theme.BoardColorUnfocused

@Composable
fun Komadai(
    piecesCount: Map<PieceKind, Int>,
    handleKomadaiClick: (PieceKind) -> Unit,
    positionToHighlight: Position,
    isEnemy: Boolean = false,
) {
    val pieceKindMap: Map<PieceKind, String> = mapOf(
        PieceKind.EMPTY to "",
        PieceKind.KING to "王",
        PieceKind.ROOK to "飛",
        PieceKind.BISHOP to "角",
        PieceKind.GOLD to "金",
        PieceKind.SILVER to "銀",
        PieceKind.KNIGHT to "桂",
        PieceKind.LANCE to "香",
        PieceKind.PAWN to "歩",
    )
    Box(
        modifier = Modifier
            .background(BoardColorUnfocused)
            .width((40 * BOARD_SIZE + ALONGBOARDSPACE).dp)
            .height(40.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(if (isEnemy) 180f else 0f)
        ) {
            items(piecesCount.keys.toList()) { pieceKind ->
                val color = if (
                    pieceKind.ordinal == positionToHighlight.column &&
                    (
                        (positionToHighlight.row == MY_KOMADAI_INDEX && !isEnemy) ||
                            (positionToHighlight.row == ENEMY_KOMADAI_INDEX && isEnemy)
                        )
                ) {
                    BoardColor
                } else {
                    BoardColorUnfocused
                }
                Box(modifier = Modifier.background(color)) {
                    Piece(
                        text = pieceKindMap[pieceKind]!!,
                        onClick = { handleKomadaiClick.invoke(pieceKind) },
                        modifier = Modifier.size(40.dp)
                    )
                    if (piecesCount[pieceKind]!! > 1) {
                        Text(text = "${piecesCount[pieceKind]!!}")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun KomadaiPreview() {
    val pieceCount = mapOf(PieceKind.PAWN to 2, PieceKind.SILVER to 1)
    Column {
        Komadai(
            piecesCount = pieceCount,
            handleKomadaiClick = {},
            positionToHighlight = Position(ENEMY_KOMADAI_INDEX, PieceKind.SILVER.ordinal),
            isEnemy = true,
        )
        Spacer(modifier = Modifier.size(10.dp))
        Komadai(
            piecesCount = pieceCount,
            handleKomadaiClick = {},
            positionToHighlight = Position(ENEMY_KOMADAI_INDEX, PieceKind.SILVER.ordinal),
        )
    }
}
