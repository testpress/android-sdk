package in.testpress.course.ui;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.core.TestpressSdk;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Video;
import in.testpress.network.RetrofitCall;

import static in.testpress.models.greendao.Content.ATTACHMENT_TYPE;
import static in.testpress.models.greendao.Content.EXAM_TYPE;
import static in.testpress.models.greendao.Content.HTML_TYPE;
import static in.testpress.models.greendao.Content.VIDEO_TYPE;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContentActivity.class, TestpressSdk.class})
public class ContentActivityTest {

    private ContentActivity contentActivity;

    @Mock
    Content content;

    Video video = new Video();

    private static final int NUMBER_OF_RETROFIT_CALLS = 9;

    @Before
    public void setUp() {
        contentActivity = PowerMockito.spy(new ContentActivity());
        doCallRealMethod().when(contentActivity).setContent((Content) any());
        contentActivity.setContent(content);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }


    @Test
    public void test_getRetrofitCalls_returnCorrectValues() {
        ContentActivity activity = new ContentActivity();
        RetrofitCall[] retrofitCalls = activity.getRetrofitCalls();
        assertThat("Check number of RetrofitCalls returned is " + NUMBER_OF_RETROFIT_CALLS,
                retrofitCalls.length,
                is(NUMBER_OF_RETROFIT_CALLS));
    }

    @Test
    public void test_checkContentType_callsLoadContentHtml_orNot_whenContentEqualsToNotes() throws Exception {
        suppress(method(ContentActivity.class, "setContentTitle", String.class));
        PowerMockito.doNothing().when(contentActivity, "hideContents");
        PowerMockito.doNothing().when(contentActivity, "loadContentHtml");
        when(content.getContentType()).thenReturn(HTML_TYPE);

        contentActivity.checkContentType();

        PowerMockito.verifyPrivate(contentActivity, times(1)).invoke("loadContentHtml");
    }

    @Test
    public void test_checkContentType_callsUpdateContent_orNot_whenRawVideoEqualsToNull() throws Exception {
        PowerMockito.doNothing().when(contentActivity, "updateContent");
        PowerMockito.doNothing().when(contentActivity, "hideContents");

        when(content.getRawVideo()).thenReturn(null);
        when(content.getContentType()).thenReturn(VIDEO_TYPE);
        contentActivity.checkContentType();

        PowerMockito.verifyPrivate(contentActivity, times(1)).invoke("updateContent");
    }

    @Test
    public void test_checkContentType_callsOnExamContent_orNot_whenContentEqualsToExam() throws Exception {
        PowerMockito.doNothing().when(contentActivity, "onExamContent");
        PowerMockito.doNothing().when(contentActivity, "hideContents");
        when(content.getContentType()).thenReturn(EXAM_TYPE);

        contentActivity.checkContentType();

        PowerMockito.verifyPrivate(contentActivity, times(1)).invoke("onExamContent");
    }

    @Test
    public void test_checkContentType_callsOnExamContent_orNot_whenContentEqualsToDisplayAttachmentContent() throws Exception {
        PowerMockito.doNothing().when(contentActivity, "hideContents");
        PowerMockito.doNothing().when(contentActivity, "displayAttachmentContent");
        when(content.getContentType()).thenReturn(ATTACHMENT_TYPE);

        contentActivity.checkContentType();

        PowerMockito.verifyPrivate(contentActivity, times(1)).invoke("displayAttachmentContent");
    }

    @Test
    public void test_checkContentType_callsInitExoPlayer_orNot_whenVideoContentIsNotDomainRestricted_and_isNonEmbeddable() throws Exception {
        video.setIsDomainRestricted(false);
        video.setUrl("Some url");
        suppress(method(ContentActivity.class, "setContentTitle", String.class));
        PowerMockito.doNothing().when(contentActivity, "initExoPlayer", anyString());
        PowerMockito.doNothing().when(contentActivity, "hideContents");
        PowerMockito.mockStatic(TestpressSdk.class);
        when(TestpressSdk.getTestpressSession((Context) any())).thenReturn(null);
        when(content.isNonEmbeddableVideo()).thenReturn(true);
        when(content.getRawVideo()).thenReturn(video);
        when(content.getContentType()).thenReturn(VIDEO_TYPE);

        contentActivity.checkContentType();

        PowerMockito.verifyPrivate(contentActivity, times(1)).invoke("initExoPlayer", anyString());
    }

}
