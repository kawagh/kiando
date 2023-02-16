package jp.kawagh.kiando

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import org.junit.Rule
import org.junit.Test
import java.io.FileOutputStream

class TakeScreenShotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun takePictureForFeatureGraphic1() {
        composeTestRule.setContent {
            KiandoM3Theme() {
                ListScreen(
                    questions = sampleQuestions,
                    navigateToQuestion = { _, _ -> {} },
                    navigateToDelete = {},
                    navigateToLicense = {},
                    handleDeleteAQuestion = {},
                    handleFavoriteQuestion = {},
                    handleRenameAQuestion = {},
                    handleLoadQuestionFromResource = {},
                    handleInsertSampleQuestions = {},
                )
            }
        }
        takeScreenShot("feature_graphic1.png")
    }

    @Test
    fun takePictureForFeatureGraphic2() {
        composeTestRule.setContent {
            KiandoM3Theme() {
                MainScreen(
                    question = sampleQuestion,
                    navigateToList = {},
                    navigateToNextQuestion = {}) { }
            }
        }
        takeScreenShot("feature_graphic2.png")
    }

    @Test
    fun takePictureForFeatureGraphic3() {
        composeTestRule.setContent {
            KiandoM3Theme() {
                MainScreen(
                    question = sampleQuestion,
                    navigateToList = {},
                    navigateToNextQuestion = {}) { }
            }
        }
        val loadedSFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/9/9/4K"
        composeTestRule.onNode(hasContentDescription("toggle decode SFEN input form"))
            .performClick()
        composeTestRule.onNode(hasContentDescription("SFEN input form"))
            .performTextInput(loadedSFEN)
        composeTestRule.onNode(hasContentDescription("load SFEN")).performClick()
        takeScreenShot("feature_graphic3.png")
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
