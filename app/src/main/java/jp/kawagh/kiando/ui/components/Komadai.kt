package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kawagh.kiando.BOARD_SIZE
import jp.kawagh.kiando.PieceKind
import jp.kawagh.kiando.ui.theme.BoardColor
import jp.kawagh.kiando.ui.theme.BoardColorUnfocused

@Composable
fun Komadai(
    piecesCount: Map<PieceKind, Int>,
    handleKomadaiClick: (PieceKind) -> Unit,
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
            .width((40 * BOARD_SIZE).dp)
            .height(40.dp)
    ) {
        LazyRow {
            items(piecesCount.keys.toList()) { pieceKind ->
                Button(
                    onClick = { handleKomadaiClick(pieceKind) },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = BoardColor,
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.width(70.dp)
                ) {
                    Text(text = pieceKindMap[pieceKind]!!)
                    Text(text = "x", fontSize = 15.sp)
                    Text(text = "${piecesCount[pieceKind]}")
                }
            }
        }
    }
}

@Preview
@Composable
fun KomadaiPreview() {
    val pieceCount = mapOf(PieceKind.PAWN to 2, PieceKind.SILVER to 1)
    Komadai(piecesCount = pieceCount, handleKomadaiClick = {})
}
