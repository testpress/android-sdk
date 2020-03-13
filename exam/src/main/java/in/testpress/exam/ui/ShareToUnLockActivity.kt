package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressSdk
import `in`.testpress.exam.R
import `in`.testpress.exam.ui.ReviewStatsFragment.MESSAGE_TO_SHARE
import `in`.testpress.exam.ui.ReviewStatsFragment.SHARE_TO_UNLOCK
import `in`.testpress.exam.ui.ReviewStatsFragment.SHARE_TO_UNLOCK_SHARED_PREFERENCE_KEY
import `in`.testpress.exam.ui.adapters.ShareToUnlockAdapter
import `in`.testpress.ui.BaseToolBarActivity
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShareToUnLockActivity : BaseToolBarActivity(), OnShareAppListener {
    private lateinit var prefs: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var shareInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_to_unlock)
        initRecyclerView()
        initSharedPreference()
        shareInfo = findViewById(R.id.share_info)
        shareInfo.typeface = TestpressSdk.getRubikMediumFont(this)
        shareInfo.text = getString(R.string.share_app_message)
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.apps_list)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = GridLayoutManager(this, getColumnCount())
        recyclerView.adapter = ShareToUnlockAdapter(getAppsList(), this.packageManager!!, this)
    }

    private fun initSharedPreference() {
        val sharedPreferenceKey = intent.getStringExtra(SHARE_TO_UNLOCK)
        prefs = getSharedPreferences(sharedPreferenceKey, Context.MODE_PRIVATE)
    }

    private fun getColumnCount(): Int = when (resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> 4
        else -> 6
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getAppsList(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_SEND, null)
        intent.type = "text/plain"
        var appsList: List<ResolveInfo> =
            this.packageManager?.queryIntentActivities(intent, 0) as List<ResolveInfo>
        appsList = sortAppsList(appsList.toMutableList())
        return appsList
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun sortAppsList(appsList: MutableList<ResolveInfo>): List<ResolveInfo> {
        val recommendedApps = arrayListOf("whatsapp", "telegram", "sms", "mms", "facebook")
        val recomendedAppsResolveInfo = recommendedApps.flatMap { packageName ->
            appsList.filter {
                it.activityInfo.packageName.contains(packageName)
            }
        }
        return (recomendedAppsResolveInfo + appsList).distinct()
    }

    override fun onResume() {
        super.onResume()
        if (prefs.getBoolean(SHARE_TO_UNLOCK_SHARED_PREFERENCE_KEY, false)) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getShareIntent(): Intent {
        val messageToShare = intent.getStringExtra(MESSAGE_TO_SHARE)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.testpress_app_name)
        intent.type = "text/html"
        intent.putExtra(Intent.EXTRA_TEXT, messageToShare)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        return intent
    }

    override fun onClick(item: ResolveInfo) {
        val activity: ActivityInfo = item.activityInfo
        val name = ComponentName(
            activity.applicationInfo.packageName,
            activity.name
        )
        val intent = getShareIntent()
        intent.component = name
        startActivity(intent)
        prefs.edit().putBoolean(SHARE_TO_UNLOCK_SHARED_PREFERENCE_KEY, true).apply()
    }
}

interface OnShareAppListener {
    fun onClick(item: ResolveInfo)
}