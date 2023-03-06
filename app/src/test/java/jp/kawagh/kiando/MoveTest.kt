package jp.kawagh.kiando

import jp.kawagh.kiando.models.Move
import jp.kawagh.kiando.models.PieceKind
import jp.kawagh.kiando.models.Position
import jp.kawagh.kiando.models.toReadable
import org.junit.Assert
import org.junit.Test

class MoveTest {
    @Test
    fun `test toReadable ７六歩`() {
        val move = Move(Position(6, 2), Position(5, 2))
        val pk = PieceKind.PAWN
        Assert.assertEquals("7六歩", move.toReadable(pk))
    }

    @Test
    fun `test toReadable 2二角成`() {
        val move = Move(Position(7, 1), Position(1, 7), isPromote = true)
        val pk = PieceKind.BISHOP
        Assert.assertEquals("2二角成", move.toReadable(pk))
    }
}