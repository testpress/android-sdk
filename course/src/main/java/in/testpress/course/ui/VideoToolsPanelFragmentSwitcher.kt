package `in`.testpress.course.ui

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import `in`.testpress.course.fragments.VideoAIFragment
import `in`.testpress.course.fragments.VideoTranscriptFragment

class VideoToolsPanelFragmentSwitcher(
    private val fragmentManager: FragmentManager,
    private val container: ViewGroup,
    private val containerId: Int,
) {
    private enum class Panel {
        AI,
        TRANSCRIPT,
    }

    fun showAi(fragment: VideoAIFragment) {
        showPanel(
            panel = Panel.AI,
            fragment = fragment,
            shouldReplace = { existing, replacement -> hasDifferentAiArgs(existing, replacement) },
        )
    }

    fun showTranscript(fragment: VideoTranscriptFragment) {
        showPanel(
            panel = Panel.TRANSCRIPT,
            fragment = fragment,
            shouldReplace = { existing, replacement -> hasDifferentTranscriptArgs(existing, replacement) },
        )
    }

    fun hideAi(remove: Boolean = false) {
        hidePanels(listOfNotNull(findPanel(Panel.AI)), remove = remove)
    }

    fun hideTranscript(remove: Boolean = false) {
        hidePanels(listOfNotNull(findPanel(Panel.TRANSCRIPT)), remove = remove)
    }

    fun hideAll(remove: Boolean = false) {
        hidePanels(listOfNotNull(findPanel(Panel.AI), findPanel(Panel.TRANSCRIPT)), remove = remove)
    }

    private fun showPanel(
        panel: Panel,
        fragment: Fragment,
        shouldReplace: (existing: Fragment, replacement: Fragment) -> Boolean,
    ) {
        container.isVisible = true

        val existing = findPanel(panel)
        fragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            val replaceExisting = existing != null && shouldReplace(existing, fragment)
            val fragmentToShow = ensurePanelFragment(panel, existing, fragment, replaceExisting, this)
            hideOtherPanel(panel, this)
            showIfHidden(fragmentToShow, this)
        }.commitNowAllowingStateLoss()

        updateContainerVisibility()
    }

    private fun hidePanels(fragments: List<Fragment>, remove: Boolean) {
        if (fragments.isEmpty()) {
            updateContainerVisibility()
            return
        }

        fragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            for (frag in fragments) {
                if (!frag.isAdded) continue
                if (remove) remove(frag) else if (!frag.isHidden) hide(frag)
            }
        }.commitNowAllowingStateLoss()

        updateContainerVisibility()
    }

    private fun findPanel(panel: Panel): Fragment? {
        return fragmentManager.findFragmentByTag(panel.tag)
    }

    private val Panel.tag: String
        get() = name

    private fun ensurePanelFragment(
        panel: Panel,
        existing: Fragment?,
        fragment: Fragment,
        replaceExisting: Boolean,
        transaction: FragmentTransaction,
    ): Fragment {
        if (existing == null) {
            transaction.add(containerId, fragment, panel.tag)
            return fragment
        }
        if (replaceExisting) {
            transaction.remove(existing)
            transaction.add(containerId, fragment, panel.tag)
            return fragment
        }
        return existing
    }

    private fun hideOtherPanel(panelToShow: Panel, transaction: FragmentTransaction) {
        val otherPanel = if (panelToShow == Panel.AI) Panel.TRANSCRIPT else Panel.AI
        val otherFragment = findPanel(otherPanel) ?: return
        if (otherFragment.isAdded && !otherFragment.isHidden) transaction.hide(otherFragment)
    }

    private fun showIfHidden(fragment: Fragment, transaction: FragmentTransaction) {
        if (fragment.isAdded && fragment.isHidden) transaction.show(fragment)
    }

    private fun updateContainerVisibility() {
        val ai = findPanel(Panel.AI)
        val transcript = findPanel(Panel.TRANSCRIPT)
        container.isVisible = (ai?.isAdded == true && !ai.isHidden) || (transcript?.isAdded == true && !transcript.isHidden)
    }

    private fun hasDifferentAiArgs(existing: Fragment, replacement: Fragment): Boolean {
        val existingAssetId = existing.arguments?.getString(VideoAIFragment.ARG_ASSET_ID).orEmpty()
        val newAssetId = replacement.arguments?.getString(VideoAIFragment.ARG_ASSET_ID).orEmpty()
        val existingNotesUrl = existing.arguments?.getString(VideoAIFragment.ARG_NOTES_URL).orEmpty()
        val newNotesUrl = replacement.arguments?.getString(VideoAIFragment.ARG_NOTES_URL).orEmpty()
        return existingAssetId != newAssetId || existingNotesUrl != newNotesUrl
    }

    private fun hasDifferentTranscriptArgs(existing: Fragment, replacement: Fragment): Boolean {
        val existingUrl = existing.arguments?.getString(VideoTranscriptFragment.ARG_SUBTITLE_URL).orEmpty()
        val newUrl = replacement.arguments?.getString(VideoTranscriptFragment.ARG_SUBTITLE_URL).orEmpty()
        return existingUrl != newUrl
    }
}
