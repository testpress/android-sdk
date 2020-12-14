package `in`.testpress.course.fragments

import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.domain.DomainContent
import org.junit.Assert.*
import org.junit.Test

class ContentFragmentFactoryTest {

    private val attachmentContent = DomainContent(1, chapterId = 1,
            active = true, contentType = "Attachment", hasStarted = true,
            isLocked = false, isScheduled = false, isCourseAvailable = false)

    private val renderableContent = DomainContent(2, chapterId = 1,
            active = true, contentType = "Attachment", hasStarted = true,
            isLocked = true, isScheduled = false, isCourseAvailable = false,
            attachment = DomainAttachmentContent(1,isRenderable = true)
    )

    @Test
    fun whenRenderableFalseAttachmentContentShouldBeReturned() {
        val fragment = ContentFragmentFactory.getFragment(content = attachmentContent)
        assertTrue(fragment is AttachmentContentFragment)
    }

    @Test
    fun whenRenderableDocumentViewerShouldBeReturned() {
        val fragment = ContentFragmentFactory.getFragment(renderableContent)
        assertTrue(fragment is DocumentViewerFragment)
    }
}
