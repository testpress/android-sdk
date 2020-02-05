package in.testpress.exam.ui;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.widget.Spinner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import in.testpress.exam.models.AttemptItem;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.util.CommonTestUtils;

import static in.testpress.models.greendao.Attempt.NOT_STARTED;
import static in.testpress.models.greendao.Attempt.RUNNING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.methodsDeclaredIn;

@RunWith(PowerMockRunner.class)
public class TestFragmentTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 6;
    private static final int NUMBER_OF_DIALOGS = 5;

    @Mock private Attempt attempt;
    @Mock private TestFragment fragment;
    @Mock private AttemptSection section;
    @Mock private AttemptSection section2;
    @Mock private List<AttemptItem> attemptItemList;
    @Mock private List<AttemptSection> sections;

    private String attemptRemainingTime = "0:00:20";
    private long attemptMillisRemaining = Long.parseLong(attemptRemainingTime.split(":")[2]);

    @Before
    public void setUp() {
        fragment.millisRemaining = -1;
        fragment.attempt = attempt;
        fragment.sections = sections;
        fragment.attemptItemList = attemptItemList;
        when(attempt.getRemainingTime()).thenReturn(attemptRemainingTime);
        when(fragment.formatMillisecond(attemptRemainingTime)).thenReturn(attemptMillisRemaining);
        when(sections.get(0)).thenReturn(section);
        when(sections.get(1)).thenReturn(section2);
    }

    @Test
    public void test_startCountDownTimer_startsNewSection_ifSectionNotStarted() {
        when(fragment.attempt.hasSectionalLock()).thenReturn(true);
        when(section.getState()).thenReturn(NOT_STARTED);

        doCallRealMethod().when(fragment).startCountDownTimer();
        fragment.startCountDownTimer();

        verify(fragment, times(1)).startSection();
    }

    @Test
    public void test_startCountDownTimer_startingTimer() {
        when(fragment.attemptItemList.isEmpty()).thenReturn(false);

        doCallRealMethod().when(fragment).startCountDownTimer();
        doCallRealMethod().when(fragment).evaluateRemainingMillisecond(anyBoolean(), anyLong(), anyLong());
        fragment.startCountDownTimer();

        assertThat("millisRemaining value needs to same as attempt remaining time",
                fragment.millisRemaining,
                is(attemptMillisRemaining));

        verify(fragment, times(1)).startCountDownTimer(attemptMillisRemaining);
    }

    @Test
    public void test_startCountDownTimer_callsTimeOver_ifRemainingTimeZero() {
        String attemptRemainingTime = "0:00:00";
        attemptMillisRemaining = 0;
        when(attempt.getRemainingTime()).thenReturn(attemptRemainingTime);
        when(fragment.formatMillisecond(attemptRemainingTime)).thenReturn(attemptMillisRemaining);

        doCallRealMethod().when(fragment).startCountDownTimer();
        fragment.startCountDownTimer();

        assertThat("millisRemaining value needs to same as attempt remaining time",
                fragment.millisRemaining,
                is(attemptMillisRemaining));

        verify(fragment, times(1)).onRemainingTimeOver();
    }

    @Test
    public void test_startCountDownTimer_usesSectionRemainingTime_forLockedSectionExam() {
        when(fragment.attempt.hasSectionalLock()).thenReturn(true);
        String sectionRemainingTime = "0:00:10";
        long sectionMillisRemaining = Long.parseLong(sectionRemainingTime.split(":")[2]);
        when(section.getRemainingTime()).thenReturn(sectionRemainingTime);
        when(fragment.formatMillisecond(sectionRemainingTime)).thenReturn(sectionMillisRemaining);
        doCallRealMethod().when(fragment).evaluateRemainingMillisecond(anyBoolean(), anyLong(), anyLong());
        when(section.getState()).thenReturn(RUNNING);

        when(fragment.attemptItemList.isEmpty()).thenReturn(false);

        doCallRealMethod().when(fragment).startCountDownTimer();
        fragment.startCountDownTimer();

        assertThat("millisRemaining value needs to same as section remaining time",
                fragment.millisRemaining,
                is(sectionMillisRemaining));

        verify(fragment, times(1)).startCountDownTimer(sectionMillisRemaining);
    }

    @Test
    public void test_startCountDownTimer_usesSectionRemainingTime_forLockedSectionExam_for_twoConsecutiveSections() {
        when(fragment.attempt.hasSectionalLock()).thenReturn(true);
        String sectionRemainingTime = "0:00:10";
        String section2RemainingTime = "0:00:15";
        long sectionMillisRemaining = Long.parseLong(sectionRemainingTime.split(":")[2]);
        when(section.getRemainingTime()).thenReturn(sectionRemainingTime);
        when(fragment.formatMillisecond(sectionRemainingTime)).thenReturn(sectionMillisRemaining);
        doCallRealMethod().when(fragment).evaluateRemainingMillisecond(anyBoolean(), anyLong(), anyLong());
        when(section.getState()).thenReturn(RUNNING);

        long section2MillisRemaining = Long.parseLong(section2RemainingTime.split(":")[2]);
        when(section2.getRemainingTime()).thenReturn(section2RemainingTime);
        when(fragment.formatMillisecond(section2RemainingTime)).thenReturn(section2MillisRemaining);
        doCallRealMethod().when(fragment).evaluateRemainingMillisecond(anyBoolean(), anyLong(), anyLong());
        when(section2.getState()).thenReturn(RUNNING);

        when(fragment.attemptItemList.isEmpty()).thenReturn(false);

        doCallRealMethod().when(fragment).startCountDownTimer();
        fragment.startCountDownTimer();

        assertThat("millisRemaining value needs to same as section remaining time",
                fragment.millisRemaining,
                is(sectionMillisRemaining));

        verify(fragment, times(1)).startCountDownTimer(sectionMillisRemaining);

        // Second section
        when(fragment.attempt.getCurrentSectionPosition()).thenReturn(1);
        fragment.startCountDownTimer();

        assertThat("millisRemaining value needs to same as section 2 remaining time",
                fragment.millisRemaining,
                is(section2MillisRemaining));

        verify(fragment, times(1)).startCountDownTimer(section2MillisRemaining);
    }

    @Test
    public void test_onSectionEnded_endExam_ifReachedLastSection() {
        int lastSectionPosition = 2;
        when(fragment.attempt.getCurrentSectionPosition()).thenReturn(lastSectionPosition);
        when(sections.size()).thenReturn(lastSectionPosition);

        doCallRealMethod().when(fragment).onSectionEnded();
        fragment.onSectionEnded();

        verify(fragment, times(1)).endExam();
    }

    @Test
    public void test_onSectionEnded_startSection_ifNextSectionAvailable() {
        int currentSectionPosition = 2;
        when(fragment.attempt.getCurrentSectionPosition()).thenReturn(currentSectionPosition);
        when(sections.size()).thenReturn(currentSectionPosition + 2);

        fragment.sectionsFilter = mock(Spinner.class);
        fragment.sectionSpinnerAdapter = mock(LockableSpinnerItemAdapter.class);

        doCallRealMethod().when(fragment).onSectionEnded();
        fragment.onSectionEnded();

        verify(fragment, times(1)).startSection();
    }

    @Test
    public void test_formatMillisecond_returnZero_ifRemainingTimeNull() {
        doCallRealMethod().when(fragment).formatMillisecond(null);

        long millisRemaining = fragment.formatMillisecond(null);

        assertThat("millisRemaining value needs to be 0",
                millisRemaining,
                is((long) 0));
    }

    @Test
    public void test_onRemainingTimeOver_endSection_ifLockedSectionExam() {
        when(fragment.attempt.hasSectionalLock()).thenReturn(true);

        doCallRealMethod().when(fragment).onRemainingTimeOver();
        fragment.onRemainingTimeOver();

        verify(fragment, times(1)).endSection();
    }

    @Test
    public void test_onRemainingTimeOver_endExam() {
        doCallRealMethod().when(fragment).onRemainingTimeOver();
        fragment.onRemainingTimeOver();

        verify(fragment, times(1)).endExam();
    }

    @Test
    public void test_stopTimer_cancel_countDownTimer() {
        CountDownTimer countDownTimer = mock(CountDownTimer.class);
        fragment.countDownTimer = countDownTimer;

        doCallRealMethod().when(fragment).stopTimer();
        fragment.stopTimer();

        verify(countDownTimer, times(1)).cancel();
        assertThat("countDownTimer must needs to be null.",
                fragment.countDownTimer,
                is(nullValue()));
    }

    @Test
    public void testTestFragment_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new TestFragment(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }

    @Test
    public void testTestFragment_getDialogs_returnCorrectValues() {
        CommonTestUtils.testGetDialogsReturnCorrectValues(
                new TestFragment(),
                NUMBER_OF_DIALOGS
        );
    }

    @Test
    public void test_onDestroy_stopTasks() {
        PowerMockito.suppress(methodsDeclaredIn(Fragment.class));

        doCallRealMethod().when(fragment).onDestroy();
        fragment.onDestroy();

        verify(fragment, times(1)).stopTimer();
        verify(fragment, times(1)).removeAppBackgroundHandler();
    }
}
