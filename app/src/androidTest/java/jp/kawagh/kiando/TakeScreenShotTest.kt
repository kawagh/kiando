package jp.kawagh.kiando

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import jp.kawagh.kiando.data.PreferencesRepository
import jp.kawagh.kiando.di.PreferenceRepositoryModule
import jp.kawagh.kiando.models.sampleQuestion
import jp.kawagh.kiando.ui.screens.ChangeLogScreen
import jp.kawagh.kiando.ui.screens.ListScreen
import jp.kawagh.kiando.ui.screens.MainScreen
import jp.kawagh.kiando.ui.screens.PreviewListScreen
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.FileOutputStream
import javax.inject.Inject

// to test using Hilt
@Suppress("unused")
class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

@UninstallModules(PreferenceRepositoryModule::class)
@HiltAndroidTest
class TakeScreenShotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)


    @Inject
    lateinit var viewModelAssistedFactory: GameViewModel.GameViewModelAssistedFactory

    @BindValue
    val preferencesRepository: PreferencesRepository = object : PreferencesRepository {
        override val filter: Flow<String> = flowOf("")
        override suspend fun setFilter(value: String) {
        }
    }

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun takePictureForFeatureGraphic1() {
        composeTestRule.activity.setContent {
            PreviewListScreen()
        }
        takeScreenShot("feature_graphic1.png")
    }

    @Test
    fun takePictureForFeatureGraphic2() {
        val viewModel = viewModelAssistedFactory.create(sampleQuestion)
        composeTestRule.activity.setContent {
            KiandoM3Theme {
                MainScreen(
                    gameViewModel = viewModel,
                    question = sampleQuestion,
                    navigateToList = {},
                    navigateToNextQuestion = {},
                    navigateToPrevQuestion = {},
                    restartQuestion = {},
                    handleUpdateQuestionDescription = {},
                )
            }
        }
        composeTestRule.onNode(hasContentDescription("6-2")).performClick()
        takeScreenShot("feature_graphic2.png")
    }

    @Test
    fun takePictureForFeatureGraphic3() {
        val viewModel = viewModelAssistedFactory.create(sampleQuestion)
        composeTestRule.activity.setContent {

            SideEffectChangeSystemUi()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                KiandoM3Theme {
                    MainScreen(
                        gameViewModel = viewModel,
                        question = sampleQuestion,
                        navigateToList = {},
                        navigateToNextQuestion = {},
                        navigateToPrevQuestion = {},
                        restartQuestion = {},
                        handleUpdateQuestionDescription = {},
                    )
                }
            }
        }
        val loadedSFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/9/9/4K"
        composeTestRule.onNode(hasContentDescription("enter in registering Question"))
            .performClick()
        composeTestRule.onNode(hasContentDescription("toggle decode SFEN input form"))
            .performClick()
        composeTestRule.onNode(hasContentDescription("SFEN input form"))
            .performTextClearance()
        composeTestRule.onNode(hasContentDescription("SFEN input form"))
            .performTextInput(loadedSFEN)
        composeTestRule.onNode(hasContentDescription("load SFEN")).performClick()
        Espresso.closeSoftKeyboard()
        takeScreenShot("feature_graphic3.png")
    }


    @Test
    fun takePictureForFeatureGraphic4() {
        val questionsViewModel = composeTestRule.activity.viewModels<QuestionsViewModel>().value
        composeTestRule.activity.setContent {
            SideEffectChangeSystemUi()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                KiandoM3Theme {
                    ListScreen(
                        questionsViewModel = questionsViewModel,
                        navigateToQuestion = { _, _ -> run {} },
                        navigateToDelete = {},
                        navigateToLicense = {},
                        navigateToChangeLog = {},
                        handleRenameQuestion = {},
                        handleRenameTag = {},
                        handleDeleteAQuestion = {}
                    )
                }
            }
        }
        composeTestRule.onNode(hasContentDescription("tags"))
            .performClick()
        takeScreenShot("feature_graphic4.png")
    }

    @Test
    fun takePictureForChangeLogScreen() {
        composeTestRule.activity.setContent {
            SideEffectChangeSystemUi()
            KiandoM3Theme {
                ChangeLogScreen(navigateToList = {})
            }
        }
        takeScreenShot("feature_graphic5.png")
    }


    // saved in <packageName>/files/
    private fun takeScreenShot(saveName: String, size: Int? = null) {
        val saveDir =
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir.canonicalPath
        val bitmap =
            if (size == null) {
                composeTestRule.onRoot().captureToImage().asAndroidBitmap()
            } else {
                composeTestRule.onRoot().captureToImage().asAndroidBitmap()
                    .let { Bitmap.createScaledBitmap(it, size, size, true) }
            }

        FileOutputStream("$saveDir/$saveName").use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
