package `in`.testpress.course.fragments

interface VideoAISidePanelContract {
    fun showVideoAISidePanel(assetId: String, notesUrl: String?)
    fun hideVideoAISidePanel(notifyHost: Boolean = true)
}
