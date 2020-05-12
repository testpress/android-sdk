package `in`.testpress.util

import android.content.Context
import android.util.Log
import io.branch.referral.Branch
import io.branch.referral.util.BranchEvent

class BranchEventTrackerFacade(val context: Context): BaseEventTrackerFacade() {
    override fun logEvent(name: String, params: HashMap<String, Any>) {
        val branchEvent = BranchEvent(name)
        for ((key, value) in params) {
            branchEvent.addCustomDataProperty(key, value.toString())
        }
        branchEvent.logEvent(context)
    }
}