package com.example.kiando

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun MainScreen() {
    Board()
}

@Composable
fun Board() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        repeat(9) {
            BoardRow()
        }
    }
}

@Composable
fun BoardRow() {
    Row() {
        repeat(9) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.size(40.dp)) {
            }
        }
    }
}
