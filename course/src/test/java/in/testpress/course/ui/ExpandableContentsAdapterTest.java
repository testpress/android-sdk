package in.testpress.course.ui;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import in.testpress.models.greendao.Chapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class ExpandableContentsAdapterTest {

    @Mock
    Chapter chapter;

    @Mock
    ExpandableContentsAdapter expandableContentsAdapter;

    @Mock
    Context context;

    @Test
    public void testConstructor_withNullValue() {

        try {
            ExpandableContentsAdapter expandableContentsAdapter = new ExpandableContentsAdapter(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testIsExpandable_whenChildrenCountLessThanOneAndContentCountGreaterThanOne_shouldReturnTrue() {
        doCallRealMethod().when(expandableContentsAdapter).isExpandable(any());
        when(chapter.getRawChildrenCount((Context) any())).thenReturn(0);
        when(chapter.getRawContentsCount((Context) any())).thenReturn(1);

        assertEquals(true, expandableContentsAdapter.isExpandable(chapter));
    }

    @Test
    public void testIsExpandable_whenContentCountLessThanOneAndChildrenCountGreaterThanOne_shouldReturnTrue() {
        doCallRealMethod().when(expandableContentsAdapter).isExpandable(any());
        when(chapter.getRawChildrenCount((Context) any())).thenReturn(1);
        when(chapter.getRawContentsCount((Context) any())).thenReturn(0);

        assertEquals(true, expandableContentsAdapter.isExpandable(chapter));
    }

    @Test
    public void testIsExpandable_whenContentCountGreaterThanOneAndChildrenCountGreaterThanOne_shouldReturnTrue() {
        doCallRealMethod().when(expandableContentsAdapter).isExpandable(any());
        when(chapter.getRawChildrenCount((Context) any())).thenReturn(1);
        when(chapter.getRawContentsCount((Context) any())).thenReturn(0);

        assertEquals(true, expandableContentsAdapter.isExpandable(chapter));
    }

    @Test
    public void testIsExpandable_whenContentCountLessThanOneAndChildrenCountLessThanOne_shouldReturnFalse() {
        doCallRealMethod().when(expandableContentsAdapter).isExpandable(any());
        when(chapter.getRawChildrenCount((Context) any())).thenReturn(0);
        when(chapter.getRawContentsCount((Context) any())).thenReturn(0);

        assertEquals(false, expandableContentsAdapter.isExpandable(chapter));
    }
}
