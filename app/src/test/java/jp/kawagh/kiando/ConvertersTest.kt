package jp.kawagh.kiando

import jp.kawagh.kiando.models.Converters
import org.junit.Test

class ConvertersTest {

    @Test
    fun fromMove() {
        val inputMove = Move(Position(2, 0), Position(2, 2), isPromote = false)
        val expectedString = "2_0_2_2_0"
        val encodedString = Converters().fromMove(inputMove)
        assert(encodedString == expectedString)

    }

    @Test
    fun fromMove2() {
        val inputMove = Move(Position(2, 0), Position(2, 2), isPromote = true)
        val expectedString = "2_0_2_2_1"
        assert(Converters().fromMove(inputMove) == expectedString)
    }


    @Test
    fun toMove() {
        val inputString = "2_0_2_2_0"
        val expectedMove = Move(Position(2, 0), Position(2, 2), isPromote = false)
        assert(Converters().toMove(inputString) == expectedMove)
    }

    @Test
    fun toMove2() {
        val inputString = "2_0_2_2_1"
        val expectedMove = Move(Position(2, 0), Position(2, 2), isPromote = true)
        assert(Converters().toMove(inputString) == expectedMove)
    }
}