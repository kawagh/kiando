package jp.kawagh.kiando.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.SideEffectChangeSystemUi
import jp.kawagh.kiando.ui.theme.KiandoM3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navigateToList: () -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "設定") }, navigationIcon = {
            IconButton(
                onClick = navigateToList
            ) {
                Icon(Icons.Default.ArrowBack, "back to list")
            }
        })
    }) { padding ->
        LazyColumn(
            Modifier
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "盤面の符号を後手目線にする")
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Switch(
                            checked = false,
                            onCheckedChange = {}
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingScreenPreview() {
    SideEffectChangeSystemUi()
    KiandoM3Theme {
        SettingScreen(navigateToList = {})
    }
}
