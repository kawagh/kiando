package jp.kawagh.kiando.data

import androidx.room.TypeConverter
import jp.kawagh.kiando.Move
import jp.kawagh.kiando.Position

class MoveConverters {
    // encode Move -> fromRow_fromCol_toRow_toCol_isPromote
    @TypeConverter
    fun fromMove(move: Move): String = StringBuilder().apply {
        append(move.from.row)
        append('_')
        append(move.from.column)
        append('_')
        append(move.to.row)
        append('_')
        append(move.to.column)
        append('_')
        if (move.isPromote) append('1') else append('0')
    }.toString()

    @TypeConverter
    fun toMove(s: String): Move {
        val tokens = s.split("_").map {
            it.toInt()
        }
        return Move(
            Position(tokens[0], tokens[1]),
            Position(tokens[2], tokens[3]),
            tokens[4] == 1
        )
    }

}
