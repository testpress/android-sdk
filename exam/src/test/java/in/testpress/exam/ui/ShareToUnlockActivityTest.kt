package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.exam.ui.ReviewStatsFragment.MESSAGE_TO_SHARE
import `in`.testpress.models.InstituteSettings
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowPackageManager
import org.robolectric.shadows.ShadowResolveInfo

@RunWith(RobolectricTestRunner::class)
class ShareToUnlockActivityTest {

    lateinit var activity: ShareToUnLockActivity
    val messageToShare = "Share this app to your friends and family"
    @Before
    fun setUp() {
        val intent = Intent()
        intent.putExtra(ReviewStatsFragment.NO_OF_TIMES_SHARED, "sharetounlock")
        intent.putExtra(MESSAGE_TO_SHARE, messageToShare)

        val instituteSettings = InstituteSettings("http://sandbox.testpress.in")
        TestpressSdk.setTestpressSession(
            ApplicationProvider.getApplicationContext<Context>(),
            TestpressSession(instituteSettings, "token")
        )
        activity = Robolectric.buildActivity(ShareToUnLockActivity::class.java, intent)
            .create()
            .resume()
            .get()
    }

    fun assertResolveInfoList(list1: List<ResolveInfo>, list2: List<ResolveInfo>): Boolean {
        return list1.mapIndexed{index, element -> element.resolvePackageName == list2[index].resolvePackageName }.all { it }
    }

    @Test
    fun shareIntentShouldContainAppropriateMessageToShare() {
        val shareIntent = activity.getShareIntent()

        Assert.assertEquals(shareIntent.action, Intent.ACTION_SEND)
        Assert.assertEquals(shareIntent.extras[EXTRA_TEXT], messageToShare)
        Assert.assertEquals(shareIntent.type, "text/html")
    }

    @Test
    fun sortAppsListShouldSortAppsAccordingly() {
        val bluetooth = ShadowResolveInfo.newResolveInfo("Bluetooth", "com.android.bluetooth")
        val notes = ShadowResolveInfo.newResolveInfo("Notes", "com.miui.notes")
        val basecamp = ShadowResolveInfo.newResolveInfo("Basecamp", "com.basecamp.bc3")
        val whatsapp = ShadowResolveInfo.newResolveInfo("Whatsapp", "com.whatsapp")
        val sms = ShadowResolveInfo.newResolveInfo("SMS", "com.google.sms")
        val facebook = ShadowResolveInfo.newResolveInfo("Messenger", "com.facebook.messenger")
        val appsList = mutableListOf(facebook, bluetooth, notes, sms, basecamp, whatsapp)

        val list = activity.sortAppsList(appsList)
        Assert.assertEquals(0, list.indexOf(whatsapp))
        Assert.assertEquals(1, list.indexOf(sms))
        Assert.assertEquals(2, list.indexOf(facebook))
    }

    @Test
    fun getAppsListShouldReturnAppInfoWithIntentActionSend() {
        val bluetooth = ShadowResolveInfo.newResolveInfo("Bluetooth", "com.android.bluetooth")
        val notes = ShadowResolveInfo.newResolveInfo("Notes", "com.miui.notes")
        val basecamp = ShadowResolveInfo.newResolveInfo("Basecamp", "com.basecamp.bc3")
        val whatsapp = ShadowResolveInfo.newResolveInfo("Whatsapp", "com.whatsapp")
        val sms = ShadowResolveInfo.newResolveInfo("SMS", "com.google.sms")
        val facebook = ShadowResolveInfo.newResolveInfo("Messenger", "com.facebook.messenger")
        val appsList = mutableListOf(facebook, bluetooth, notes, sms, basecamp, whatsapp)

        val intent = Intent(Intent.ACTION_SEND)
        val a = ShadowPackageManager()
        a.addResolveInfoForIntent(intent, appsList)

        assertResolveInfoList(appsList, activity.getAppsList())
    }
}