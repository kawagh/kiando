package jp.kawagh.kiando

import jp.kawagh.kiando.ui.screens.ChangeLogs
import jp.kawagh.kiando.ui.screens.ReleaseLog
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

// TODO find way to exclude this test by `./gradlew test` and way to run this test.
@Ignore("want to run only before release")
class ReleaseReadyTest {
    @Test()
    fun confirmReleaseLogIsFirst() {
        Assert.assertTrue(ChangeLogs.data.first() is ReleaseLog)
    }

    @Test
    fun releaseVersionCheck() {
        val versionInChangeLog = ((ChangeLogs.data.first()) as ReleaseLog).version
        Assert.assertEquals(versionInChangeLog+'a', BuildConfig.VERSION_NAME)
    }
}