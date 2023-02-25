package jp.kawagh.kiando

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.models.Tag
import jp.kawagh.kiando.ui.components.QuestionWithTagsCard
import jp.kawagh.kiando.ui.components.TagChip
import jp.kawagh.kiando.ui.theme.CardColor
import kotlinx.coroutines.launch

//@Preview
//@Composable
//fun PreviewListScreen() {
//    ListScreen(
//        QuestionsUiState(
//            sampleQuestions.map { QuestionWithTags(it, emptyList()) }
//        ),
//        { _, _ -> {} }, {}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> {} }, {})
//
//}

enum class TabItem {
    All,
    Favorite,
}

enum class BottomBarItems(val title: String, val icon: ImageVector) {
    Questions(title = "questions", icon = Icons.Default.QuestionMark),
    Tags(title = "tags", icon = Icons.Default.Tag),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    questionsViewModel: QuestionsViewModel,
    navigateToQuestion: (Question, Int) -> Unit,
    navigateToDelete: () -> Unit,
    navigateToLicense: () -> Unit,
    handleRenameQuestion: (Question) -> Unit,
    handleRenameTag: (Tag) -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
    handleFavoriteQuestion: (Question) -> Unit,
    handleInsertSampleQuestions: () -> Unit,
    handleLoadDataFromResource: () -> Unit,
    handleAddTag: (Tag) -> Unit,
    handleToggleCrossRef: (Question, Tag) -> Unit,
    handleRemoveTagById: (Int) -> Unit
) {
    val questionsUiState = questionsViewModel.uiState
    val navigateToQuestionWithTabIndex: (Question) -> Unit = {
        navigateToQuestion(it, questionsUiState.tabRowIndex)
    }

    var selectedFilterTag: Tag? by remember {
        mutableStateOf(null)
    }
    val questionsToDisplay = when (TabItem.values()[questionsUiState.tabRowIndex]) {
        TabItem.All -> questionsUiState.questionsWithTags
        TabItem.Favorite -> questionsUiState.questionsWithTags.filter { it.question.isFavorite }
    }.filter {
        if (questionsUiState.hideDefaultQuestions) {
            it.question.id >= 0
        } else {
            true
        }
    }.filter {
        if (selectedFilterTag != null) {
            it.tags.contains(selectedFilterTag)
        } else {
            true
        }
    }
    var dropDownExpanded by remember {
        mutableStateOf(false)
    }

    val dropDownMenuItems: Map<String, () -> Unit> =
        mapOf(stringResource(R.string.no_filter_name) to { selectedFilterTag = null }) +
                questionsUiState.tags.associate { it.title to { selectedFilterTag = it } }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                hideDefaultQuestions = questionsUiState.hideDefaultQuestions,
                toggleHideDefaultQuestions = { questionsViewModel.toggleHideDefaultQuestions() },
                handleInsertSampleQuestions = handleInsertSampleQuestions,
                navigateToDelete = navigateToDelete,
                navigateToLicense = navigateToLicense,
                handleLoadDataFromResource = handleLoadDataFromResource,
            )
        }) {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        when (BottomBarItems.values()[questionsUiState.bottomBarIndex]) {
                            BottomBarItems.Questions -> "問題一覧"
                            BottomBarItems.Tags -> if (questionsUiState.isTagEditMode) {
                                "タグ編集"
                            } else {
                                "タグ一覧"
                            }
                        }
                    )
                },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    actions = {
                        when (BottomBarItems.values()[questionsUiState.bottomBarIndex]) {
                            BottomBarItems.Questions -> {
                                if (dropDownMenuItems.isNotEmpty()) {
                                    IconToggleButton(
                                        checked = selectedFilterTag is Tag,
                                        onCheckedChange = {
                                            dropDownExpanded = !dropDownExpanded
                                        }) {
                                        Icon(Icons.Default.FilterAlt, null)
                                    }
                                    DropdownMenuOnTopBar(
                                        dropDownMenuItems,
                                        expanded = dropDownExpanded,
                                        selectedName = selectedFilterTag?.title
                                            ?: stringResource(id = R.string.no_filter_name),
                                        setExpanded = { dropDownExpanded = it })
                                }
                            }

                            BottomBarItems.Tags -> {
                                IconToggleButton(
                                    checked = questionsUiState.isTagEditMode,
                                    onCheckedChange = { questionsViewModel.toggleTagEditMode() }) {
                                    Icon(Icons.Default.Edit, "edit tags")
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    BottomBarItems.values().forEachIndexed { index, bottomBarItem ->
                        NavigationBarItem(
                            selected = questionsUiState.bottomBarIndex == index,
                            onClick = { questionsViewModel.setBottomBarIndex(index) },
                            label = { Text(bottomBarItem.title) },
                            icon = { Icon(bottomBarItem.icon, null) })

                    }
                }
            }
        ) { paddingValues ->
            when (BottomBarItems.values()[questionsUiState.bottomBarIndex]) {
                BottomBarItems.Questions -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TabRow(selectedTabIndex = questionsUiState.tabRowIndex) {
                            TabItem.values().forEachIndexed { index, tab ->
                                Tab(
                                    selected = questionsUiState.tabRowIndex == index,
                                    onClick = { questionsViewModel.setTabRowIndex(index) }) {
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
                                handleDeleteQuestion = handleDeleteAQuestion,
                                handleRenameQuestion = handleRenameQuestion,
                                handleFavoriteQuestion = handleFavoriteQuestion,
                            )
                        }
                    }

                }

                BottomBarItems.Tags -> {
                    if (questionsUiState.isTagEditMode) {
                        TagsContentOnEditMode(
                            questionsUiState.tags,
                            handleRenameTag,
                            handleRemoveTagById,
                            paddingValues
                        )
                    } else {
                        // modes are Edit,List
                        TagsContentOnListMode(
                            tags = questionsUiState.tags,
                            questionsWithTags = questionsUiState.questionsWithTags,
                            handleAddTag = handleAddTag,
                            handleToggleCrossRef = handleToggleCrossRef,
                            paddingValues = paddingValues,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    hideDefaultQuestions: Boolean,
    toggleHideDefaultQuestions: () -> Unit,
    handleInsertSampleQuestions: () -> Unit,
    navigateToDelete: () -> Unit,
    navigateToLicense: () -> Unit,
    handleLoadDataFromResource: () -> Unit,
) {
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
                onCheckedChange = { toggleHideDefaultQuestions() })
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
                onClick = handleLoadDataFromResource
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsContentOnListMode(
    tags: List<Tag>,
    questionsWithTags: List<QuestionWithTags>,
    handleAddTag: (Tag) -> Unit,
    handleToggleCrossRef: (Question, Tag) -> Unit,
    paddingValues: PaddingValues,
) {
    val questionOptions = questionsWithTags.map { it.question }

    /**
     * states for tags
     */
    var questionsDropdownExpanded by remember {
        mutableStateOf(false)
    }
    var tagNameInput by remember {
        mutableStateOf("")
    }
    var selectedQuestion by remember {
        mutableStateOf(
            if (questionsWithTags.isEmpty()) {
                null
            } else {
                sampleQuestion
            }
        )
    }
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

        item {
            Spacer(modifier = Modifier.size(24.dp))
            Divider()
            Spacer(modifier = Modifier.size(24.dp))
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

        item { Text("tags") }

        item {
            val handleTagClick: (Tag) -> Unit = { tag ->
                selectedQuestion?.let {
                    handleToggleCrossRef(it, tag)
                }

            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedQuestion == null) {
                    items(tags) {
                        TagChip(tag = it, { handleTagClick(it) })
                    }
                } else {
                    val tagsAttachedSelectedQuestions =
                        questionsWithTags
                            .find { it.question == selectedQuestion }?.tags
                            ?: emptyList()
                    items(tags) {
                        TagChip(
                            tag = it,
                            onClick = { handleTagClick(it) },
                            rippleEnabled = true,
                            containerColor = if (tagsAttachedSelectedQuestions.contains(
                                    it
                                )
                            ) {
                                CardColor
                            } else {
                                Color.Transparent
                            },
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TagsContentOnEditMode(
    tags: List<Tag>,
    handleRenameTag: (Tag) -> Unit,
    handleRemoveTagById: (Int) -> Unit,
    paddingValues: PaddingValues,
) {
    val tagIdsToDelete = remember {
        mutableStateListOf<Int>()
    }
    LazyColumn(
        Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(tags) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 4.dp)
                .clickable { handleRenameTag(it) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        Text(
                            it.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = tagIdsToDelete.contains(it.id),
                            onCheckedChange = { on ->
                                if (on) {
                                    tagIdsToDelete.add(it.id)
                                } else {
                                    tagIdsToDelete.remove(it.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    tagIdsToDelete.forEach { handleRemoveTagById(it) }
                    tagIdsToDelete.clear()
                },
                enabled = tagIdsToDelete.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Icon(
                    Icons.Default.Delete, null,
                    modifier = Modifier.size(
                        ButtonDefaults.IconSize
                    )
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("選択したタグを削除")
            }
        }
    }
}

@Composable
fun DropdownMenuOnTopBar(
    dropDownMenuItems: Map<String, () -> Unit>,
    expanded: Boolean,
    selectedName: String,
    setExpanded: (Boolean) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
        dropDownMenuItems.forEach { (name, callback) ->
            DropdownMenuItem(text = {
                Text(
                    text = name,
                    modifier = Modifier.background(
                        if (name == selectedName) Color.LightGray else {
                            Color.Transparent
                        }
                    )
                )
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
    handleDeleteQuestion: (Question) -> Unit,
    handleRenameQuestion: (Question) -> Unit,
    handleFavoriteQuestion: (Question) -> Unit
) {
    LazyColumn {
        items(questionsWithTags) { questionWithTags ->
            QuestionWithTagsCard(
                questionWithTags = questionWithTags,
                onClick = { navigateToQuestion(questionWithTags.question) },
                handleDeleteQuestion = { handleDeleteQuestion(questionWithTags.question) },
                handleFavoriteQuestion = { handleFavoriteQuestion(questionWithTags.question) },
                handleRenameQuestion = { handleRenameQuestion(questionWithTags.question) },
            )
            Spacer(Modifier.size(5.dp))
        }
    }
}