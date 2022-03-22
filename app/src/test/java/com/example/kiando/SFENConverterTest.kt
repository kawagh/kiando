package com.example.kiando

import org.junit.Assert.*

import org.junit.Test

class SFENConverterTest {

    @Test
    fun testConvertFrom() {
        val initialSFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL"
        val decodedBoard = SFENConverter().convertFrom(initialSFEN)
        val expected = PanelState(0, 0, PieceKind.LANCE, true)
        assert(expected == decodedBoard[0]) {
            "Decode Failed at: ${decodedBoard[0]}"
        }
        val expected01 = PanelState(0, 1, PieceKind.KNIGHT, true)
        assert(expected01 == decodedBoard[1]) {
            "Decode Failed at: ${decodedBoard[1]}"
        }
        val expected11 = PanelState(1, 1, PieceKind.ROOK, true)
        assert(expected11 == decodedBoard[10]) {
            "Decode Failed at: ${decodedBoard[10]}"
        }
        assert(decodedBoard == initialBoardState.flatten()) {
            "$decodedBoard"
        }
    }

}