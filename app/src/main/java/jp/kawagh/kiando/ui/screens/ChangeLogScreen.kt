package jp.kawagh.kiando.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.BuildConfig
import jp.kawagh.kiando.SideEffectChangeSystemUi
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import java.time.LocalDate

sealed interface BaseChangeLog {
    val date: LocalDate
}

data class ChangeLog(val title: String, override val date: LocalDate) : BaseChangeLog
data class ReleaseLog(val version: String, override val date: LocalDate) : BaseChangeLog

object ChangeLogs {
    // want to manage only data or `CHANGELOG.md`
    val data: List<BaseChangeLog> =
        listOf(
            ChangeLog(title = "盤面の符号の反転機能の追加", date = LocalDate.of(2023, 7, 15)),
            ChangeLog(title = "削除、名称変更のダイアログで問題名の表示", date = LocalDate.of(2023, 7, 13)),
            ChangeLog(title = "ダイアログ表示後の問題一覧からの遷移の改善", date = LocalDate.of(2023, 7, 13)),
            ChangeLog(title = "アプリアイコンの変更", date = LocalDate.of(2023, 7, 13)),
            ChangeLog(title = "SFEN入力フォーム以下の要素が重なる問題の修正", date = LocalDate.of(2023, 7, 12)),
            ChangeLog(title = "問題の解説の表示、編集機能の追加", date = LocalDate.of(2023, 7, 11)),
            ReleaseLog(version = "1.0.19", date = LocalDate.of(2023, 7, 7)),
            ChangeLog(title = "更新履歴の表示", date = LocalDate.of(2023, 7, 7)),
            ChangeLog(title = "問題読み込み時のちらつきの防止", date = LocalDate.of(2023, 7, 7)),
            ChangeLog(title = "問題のリセット機能の追加", date = LocalDate.of(2023, 7, 6)),
            ChangeLog(title = "問題非表示時のメッセージの表示", date = LocalDate.of(2023, 6, 24)),
            ChangeLog(title = "問題絞り込み時の遷移先の問題の改善", date = LocalDate.of(2023, 6, 24)),
            ReleaseLog(version = "1.0.18", date = LocalDate.of(2023, 6, 23)),
            ChangeLog(title = "将棋盤に符号の追加", date = LocalDate.of(2023, 6, 23)),
            ReleaseLog(version = "1.0.17", date = LocalDate.of(2023, 3, 19)),
            ChangeLog(title = "二歩の検知", date = LocalDate.of(2023, 3, 19)),
            ChangeLog(title = "駒台に関する不具合の修正", date = LocalDate.of(2023, 3, 19)),
            ChangeLog(title = "成不成のダイアログの改善", date = LocalDate.of(2023, 3, 17)),
        )
}

@Preview
@Composable
fun ChangeLogScreenPreview() {
    SideEffectChangeSystemUi()
    KiandoM3Theme {
        ChangeLogScreen(navigateToList = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeLogScreen(navigateToList: () -> Unit) {
    val dateGroupedChangeLogs = ChangeLogs.data.groupBy { it.date }
    val versionName = BuildConfig.VERSION_NAME
    val nextVersionName =
        (
            versionName.split(".").take(2) +
                (versionName.split(".").last().toInt() + 1).toString()
            ).joinToString(".")
    val latestReleaseDate =
        ChangeLogs.data.filterIsInstance<ReleaseLog>().maxByOrNull { it.date }?.date
            ?: LocalDate.MIN
    val hasUnreleasedChangeLogs =
        ChangeLogs.data.filterIsInstance<ChangeLog>().any { it.date > latestReleaseDate }

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "更新履歴") }, navigationIcon = {
            IconButton(
                onClick = navigateToList
            ) {
                Icon(Icons.Default.ArrowBack, "back to list")
            }
        })
    }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (hasUnreleasedChangeLogs) {
                item {
                    Text(
                        "$nextVersionName (Not released)",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            items(
                dateGroupedChangeLogs.entries.toList()
                    .sortedByDescending { it.key }
            ) { dateToLogs ->
                Column {
                    val releaseLog = dateToLogs.value.filterIsInstance<ReleaseLog>().firstOrNull()
                    val isReleased = ChangeLogs.data.filterIsInstance<ReleaseLog>()
                        .find { it.date >= dateToLogs.key } != null
                    val alpha = if (isReleased) 1f else 0.5f
                    releaseLog?.let {
                        Text(
                            "${it.version} (${it.date})",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Text(
                        dateToLogs.key.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alpha(alpha)
                    )
                    dateToLogs.value.forEach {
                        when (it) {
                            is ChangeLog -> Text(
                                text = "- ${it.title}",
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .alpha(alpha)
                            )

                            is ReleaseLog -> {}
                        }
                    }
                }
            }
        }
    }
}
