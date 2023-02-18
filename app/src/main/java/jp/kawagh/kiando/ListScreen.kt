package jp.kawagh.kiando

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.models.Tag
import jp.kawagh.kiando.ui.components.QuestionWithTagsCard
import jp.kawagh.kiando.ui.components.TagChip
import jp.kawagh.kiando.ui.theme.BoardColor
import kotlinx.coroutines.launch

@Preview
@Composable
fun PreviewListScreen() {
    ListScreen(
        QuestionsUiState(
            sampleQuestions.map { QuestionWithTags(it, emptyList()) }
        ),
        { _, _ -> {} }, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> {} })

}

sealed class TabItem(val name: String) {
    object All : TabItem("All")
    object Favorite : TabItem("Favorite")
}

enum class BottomBarItems(val title: String, val icon: ImageVector) {
    Questions(title = "questions", icon = Icons.Default.QuestionMark),
    Tags(title = "tags", icon = Icons.Default.Tag),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    questionsUiState: QuestionsUiState,
    navigateToQuestion: (Question, Int) -> Unit,
    navigateToDelete: () -> Unit,
    navigateToLicense: () -> Unit,
    handleRenameAQuestion: (Question) -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
    handleFavoriteQuestion: (Question) -> Unit,
    handleInsertSampleQuestions: () -> Unit,
    handleLoadQuestionFromResource: () -> Unit,
    handleAddTag: (Tag) -> Unit,
    handleAddCrossRef: (Question, Tag) -> Unit,
) {
    var tabRowIndex by remember {
        mutableStateOf(0)
    }
    var bottomBarIndex by remember {
        mutableStateOf(0)
    }
    val navigateToQuestionWithTabIndex: (Question) -> Unit = {
        navigateToQuestion(it, tabRowIndex)
    }
    val tabs = listOf(TabItem.All, TabItem.Favorite)

    var hideDefaultQuestions by remember {
        mutableStateOf(false)
    }
    val questionsToDisplay = when (tabs[tabRowIndex]) {
        is TabItem.All -> questionsUiState.questionsWithTags
        is TabItem.Favorite -> questionsUiState.questionsWithTags.filter { it.question.tag_id != null }
    }.filter {
        if (hideDefaultQuestions) {
            it.question.id >= 0
        } else {
            true
        }
    }
    var dropDownExpanded by remember {
        mutableStateOf(false)
    }

    val dropDownMenuItems = emptyMap<String, () -> Unit>()
//    issues/92 questionFilter
//    val dropDownMenuItems = mapOf(
//        "Delete Questions" to navigateToDelete,
//        "License" to navigateToLicense,
//    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    /**
     * states for tags
     */
    var tagNameInput by remember {
        mutableStateOf("")
    }
    var tagDropDownExpanded by remember {
        mutableStateOf(false)
    }
    val tagOptions = questionsUiState.tags
    var selectedTag by remember {
        mutableStateOf(
            if (questionsUiState.tags.isEmpty()) {
                null
            } else {
                Tag(-1, "sample")
            }
        )
    }
    var questionsDropdownExpanded by remember {
        mutableStateOf(false)
    }
    val questionOptions = questionsUiState.questionsWithTags.map { it.question }
    var selectedQuestion by remember {
        mutableStateOf(
            if (questionsUiState.questionsWithTags.isEmpty()) {
                null
            } else {
                sampleQuestion
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "カスタム将棋次の一手",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp)
                )
                Divider(Modifier.padding(8.dp))
                Text(
                    "Version: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
                Divider(Modifier.padding(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("hide default questions", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.size(12.dp))
                    Switch(
                        checked = hideDefaultQuestions,
                        onCheckedChange = { hideDefaultQuestions = !hideDefaultQuestions })
                }
                Divider(Modifier.padding(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, null) },
                    label = { Text("add sample questions") },
                    selected = false,
                    onClick = handleInsertSampleQuestions
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Android, null) },
                    label = { Text("License") },
                    selected = false,
                    onClick = navigateToLicense
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Warning, null) },
                    label = { Text("Delete All Questions") },
                    selected = false,
                    onClick = navigateToDelete
                )
                if (BuildConfig.DEBUG) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.LogoDev, null) },
                        label = { Text("load questions from resource") },
                        selected = false,
                        onClick = handleLoadQuestionFromResource
                    )
                }
            }
        }) {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        when (BottomBarItems.values()[bottomBarIndex]) {
                            BottomBarItems.Questions -> "問題一覧"
                            BottomBarItems.Tags -> "タグ一覧"
                        }
                    )
                },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    actions = {
                        if (dropDownMenuItems.isNotEmpty()) {
                            IconButton(onClick = { dropDownExpanded = !dropDownExpanded }) {
                                Icon(Icons.Default.MoreVert, null)
                            }
                            DropdownMenuOnTopBar(
                                dropDownMenuItems,
                                expanded = dropDownExpanded,
                                setExpanded = { dropDownExpanded = it })
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    BottomBarItems.values().forEachIndexed { index, bottomBarItem ->
                        NavigationBarItem(
                            selected = bottomBarIndex == index,
                            onClick = { bottomBarIndex = index },
                            label = { Text(bottomBarItem.title) },
                            icon = { Icon(bottomBarItem.icon, null) })

                    }
                }
            }
        ) { paddingValues ->
            when (BottomBarItems.values()[bottomBarIndex]) {
                BottomBarItems.Questions -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TabRow(selectedTabIndex = tabRowIndex) {
                            tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = tabRowIndex == index,
                                    onClick = { tabRowIndex = index }) {
                                    Text(
                                        tab.name,
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.padding(top = 12.dp))
                        if (questionsUiState.questionsWithTags.isEmpty()) {
                            Box(Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        "no questions",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Button(onClick = handleInsertSampleQuestions) { Text("add sample questions") }
                                }
                            }
                        } else {
                            QuestionsList(
                                questionsWithTags = questionsToDisplay,
                                navigateToQuestion = navigateToQuestionWithTabIndex,
                                handleDeleteAQuestion = handleDeleteAQuestion,
                                handleRenameAQuestion = handleRenameAQuestion,
                                handleFavoriteQuestion = handleFavoriteQuestion,
                            )
                        }
                    }

                }

                BottomBarItems.Tags -> {
                    LazyColumn(
                        Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = tagNameInput,
                                onValueChange = { tagNameInput = it },
                                label = { Text("tag") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (tagNameInput.isNotEmpty()) {
                                            handleAddTag(Tag(title = tagNameInput))
                                        }
                                    }) {
                                        Icon(Icons.Default.Add, null)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )

                        }

                        item { Text("tags") }

                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(questionsUiState.tags) {
                                    TagChip(tag = it)
                                }
                            }

                        }

                        item {
                            Divider()
                        }

                        item {
                            ExposedDropdownMenuBox(
                                expanded = questionsDropdownExpanded,
                                onExpandedChange = {
                                    questionsDropdownExpanded = !questionsDropdownExpanded
                                }) {
                                OutlinedTextField(
                                    value = selectedQuestion?.description ?: "問題を選択してください",
                                    onValueChange = {},
                                    label = { Text("問題") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = questionsDropdownExpanded
                                        )
                                    },
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = questionsDropdownExpanded,
                                    onDismissRequest = { questionsDropdownExpanded = false }) {
                                    questionOptions.forEach {
                                        DropdownMenuItem(
                                            text = { Text(it.description) },
                                            onClick = {
                                                selectedQuestion = it
                                                questionsDropdownExpanded = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }

                            }
                        }

                        item {
                            ExposedDropdownMenuBox(
                                expanded = tagDropDownExpanded,
                                onExpandedChange = {
                                    tagDropDownExpanded = !tagDropDownExpanded
                                }) {
                                OutlinedTextField(
                                    value = selectedTag?.title ?: "タグを選択してください",
                                    onValueChange = {},
                                    label = { Text("タグ") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = tagDropDownExpanded
                                        )
                                    },
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = tagDropDownExpanded,
                                    onDismissRequest = { tagDropDownExpanded = false }) {
                                    tagOptions.forEach {
                                        DropdownMenuItem(
                                            text = { Text(it.title) },
                                            onClick = {
                                                selectedTag = it
                                                tagDropDownExpanded = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(onClick = {
                                    selectedQuestion?.let { question ->
                                        selectedTag?.let { tag ->
                                            handleAddCrossRef(question, tag)
                                        }
                                    }
                                })
                                {
                                    Text("問題にタグを追加")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuOnTopBar(
    dropDownMenuItems: Map<String, () -> Unit>,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
        dropDownMenuItems.forEach { (name, callback) ->
            DropdownMenuItem(text = {
                Text(text = name)
            }, onClick = {
                callback.invoke()
                setExpanded(false)
            })
        }
    }
}


@Composable
fun QuestionsList(
    questionsWithTags: List<QuestionWithTags>,
    navigateToQuestion: (Question) -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
    handleRenameAQuestion: (Question) -> Unit,
    handleFavoriteQuestion: (Question) -> Unit
) {
    LazyColumn {
        items(questionsWithTags) { questionWithTags ->
            QuestionWithTagsCard(
                questionWithTags = questionWithTags,
                onClick = { navigateToQuestion(questionWithTags.question) },
                handleDeleteAQuestion = { handleDeleteAQuestion(questionWithTags.question) },
                handleFavoriteQuestion = { handleFavoriteQuestion(questionWithTags.question) },
                handleRenameAQuestion = { handleRenameAQuestion(questionWithTags.question) },
            )
            Spacer(Modifier.size(5.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionRow(
    question: Question, onClick: () -> Unit,
    handleDeleteAQuestion: () -> Unit,
    handleFavoriteQuestion: () -> Unit,
) {
    var showButtons by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showButtons = !showButtons },
            )
            .background(BoardColor),
        horizontalArrangement = if (showButtons) Arrangement.Start else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        if (showButtons) {
            IconButton(
                onClick = { handleFavoriteQuestion.invoke() },
            ) {
                Icon(
                    if (question.tag_id == 1) Icons.Default.Star else Icons.Default.Remove,
                    "toggle favorite",
                )
            }
            IconButton(onClick = {
                handleDeleteAQuestion.invoke()
                showButtons = false
            }) {
                Icon(Icons.Default.Delete, "delete")
            }
        }
        Text(text = question.description, fontSize = MaterialTheme.typography.titleLarge.fontSize)
    }
}

@Composable
fun StableQuestionRow(question: Question, onClick: () -> Unit, handleDeleteAQuestion: () -> Unit) {
    Row(
        modifier = Modifier
            .width(300.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .clickable { onClick.invoke() }
            .background(BoardColor),
        horizontalArrangement = Arrangement.Center,
    )
    {
        Text(text = question.description, fontSize = MaterialTheme.typography.titleLarge.fontSize)
    }
}
