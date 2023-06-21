package jp.kawagh.kiando

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import jp.kawagh.kiando.models.sampleQuestion
import jp.kawagh.kiando.ui.screens.MainScreen
import jp.kawagh.kiando.ui.screens.PreviewListScreen
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.FileOutputStream
import javax.inject.Inject

// to test using Hilt
class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

@HiltAndroidTest
class TakeScreenShotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)


    @Inject
    lateinit var viewModelAssistedFactory: GameViewModel.GameViewModelAssistedFactory

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun takePictureForFeatureGraphic1() {
        composeTestRule.setContent {
            PreviewListScreen()
        }
        takeScreenShot("feature_graphic1.png")
    }

    @Test
    fun takePictureForFeatureGraphic2() {
        val viewModel = viewModelAssistedFactory.create(sampleQuestion)
        composeTestRule.setContent {
            KiandoM3Theme {
                MainScreen(
                    gameViewModel = viewModel,
                    question = sampleQuestion,
                    navigateToList = {},
                    navigateToNextQuestion = {},
                    navigateToPrevQuestion = {},
                )
            }
        }
        takeScreenShot("feature_graphic2.png")
    }

//    @Test
//    fun takePictureForFeatureGraphic3() {
//        composeTestRule.setContent {
//            KiandoM3Theme() {
//                MainScreen(
//                    question = sampleQuestion,
//                    navigateToList = {},
//                    navigateToNextQuestion = {}) { }
//            }
//        }
//        val loadedSFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/9/9/4K"
//        composeTestRule.onNode(hasContentDescription("toggle decode SFEN input form"))
//            .performClick()
//        composeTestRule.onNode(hasContentDescription("SFEN input form"))
//            .performTextInput(loadedSFEN)
//        composeTestRule.onNode(hasContentDescription("load SFEN")).performClick()
//        takeScreenShot("feature_graphic3.png")
//    }

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
