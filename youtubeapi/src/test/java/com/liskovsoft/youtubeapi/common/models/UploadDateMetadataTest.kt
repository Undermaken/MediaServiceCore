package com.liskovsoft.youtubeapi.common.models

import android.app.Application
import com.google.gson.Gson
import com.liskovsoft.youtubeapi.common.models.gen.ItemWrapper
import com.liskovsoft.youtubeapi.common.models.impl.mediaitem.WrapperMediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE, application = Application::class)
class UploadDateMetadataTest {
    @Test
    fun tvVideoTileExposesDateWithoutChangingItsSubtitle() {
        val item = tile("""
            {"lineItemRenderer":{"text":{"simpleText":"17 mil visualizaciones","accessibility":{"accessibilityData":{"label":"17 mil visualizaciones"}}}}},
            {"lineItemRenderer":{"text":{"simpleText":"•"}}},
            {"lineItemRenderer":{"text":{"simpleText":"hace 5 horas"}}}
        """)
        assertEquals("hace 5 horas", item.uploadDateText)
        assertTrue(item.secondTitle.toString().contains("17 mil visualizaciones • hace 5 horas"))
    }

    @Test
    fun viewsOnlyAndPlaylistTilesHaveNoUploadDate() {
        assertNull(tile("""{"lineItemRenderer":{"text":{"simpleText":"17K views"}}}""").uploadDateText)
        assertNull(tile("""
            {"lineItemRenderer":{"text":{"simpleText":"17 videos","accessibility":{"accessibilityData":{"label":"17 videos"}}}}},
            {"lineItemRenderer":{"text":{"simpleText":"•"}}},
            {"lineItemRenderer":{"text":{"simpleText":"Playlist"}}}
        """, "TILE_CONTENT_TYPE_PLAYLIST").uploadDateText)
    }

    @Test
    fun searchVideoAndMusicLockupsExposeTheirLabeledDates() {
        val fields = """{"text":{"content":"7.6K views"}},
                        {"text":{"content":"2 hours ago"},"accessibilityLabel":"2 hours ago"}"""
        for (type in listOf("LOCKUP_CONTENT_TYPE_VIDEO", "LOCKUP_CONTENT_TYPE_MUSIC")) {
            val item = lockup(fields, type)
            assertEquals("2 hours ago", item.uploadDateText)
            assertTrue(item.secondTitle.toString().contains("7.6K views • 2 hours ago"))
        }
    }

    @Test
    fun musicLockupWithSongAndViewsDoesNotTreatViewsAsDate() {
        assertNull(lockup("""{"text":{"content":"Song"}},{"text":{"content":"271K views"}}""",
            "LOCKUP_CONTENT_TYPE_MUSIC").uploadDateText)
    }

    private fun lockup(fields: String, type: String): WrapperMediaItem {
        val json = """
            {"lockupViewModel":{"contentType":"$type","metadata":{"lockupMetadataViewModel":{"metadata":{
                "contentMetadataViewModel":{"metadataRows":[
                    {"metadataParts":[{"text":{"content":"Channel"}}]},
                    {"metadataParts":[$fields]}
                ]}
            }}}}}
        """
        return WrapperMediaItem(Gson().fromJson(json, ItemWrapper::class.java))
    }

    private fun tile(fields: String, type: String = "TILE_CONTENT_TYPE_VIDEO"): WrapperMediaItem {
        val json = """
            {"tileRenderer":{"contentType":"$type","metadata":{"tileMetadataRenderer":{"lines":[
                {"lineRenderer":{"items":[{"lineItemRenderer":{"text":{"simpleText":"Channel"}}}]}},
                {"lineRenderer":{"items":[$fields]}}
            ]}}}}
        """
        return WrapperMediaItem(Gson().fromJson(json, ItemWrapper::class.java))
    }
}
