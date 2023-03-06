package jp.kawagh.kiando

import jp.kawagh.kiando.models.MoveConverters
import org.junit.Test

class MoveConvertersTest {

    @Test
    fun fromMove() {
        val inputMove = Move(Position(2, 0), Position(2, 2), isPromote = false)
        val expectedString = "2_0_2_2_0"
        val encodedString = MoveConverters().fromMove(inputMove)
        assert(encodedString == expectedString)

    }

    @Test
    fun fromMove2() {
        val inputMove = Move(Position(2, 0), Position(2, 2), isPromote = true)
        val expectedString = "2_0_2_2_1"
        assert(MoveConverters().fromMove(inputMove) == expectedString)
    }


    @Test
    fun toMove() {
        val inputString = "2_0_2_2_0"
        val expectedMove = Move(Position(2, 0), Position(2, 2), isPromote = false)
        assert(MoveConverters().toMove(inputString) == expectedMove)
    }

    @Test
    fun toMove2() {
        val inputString = "2_0_2_2_1"
        val expectedMove = Move(Position(2, 0), Position(2, 2), isPromote = true)
        assert(MoveConverters().toMove(inputString) == expectedMove)
    }
}