package in.testpress.models;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;

import in.testpress.models.greendao.Chapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class ChapterTest {

    Chapter chapter = new Chapter();

    @Test
    public void testChapter_getRawChildrenCount_returnsRawChildrenCount_ifRawChildrenCountAlreadyExist() {
        Integer rawChildren = 10;
        chapter.setChildrenCount(rawChildren);

        assertEquals(rawChildren, chapter.getChildrenCount());
    }

    @Test
    public void testChapterGetRawContentsCount_returnsRawContentCount_ifRawContainsCountAlreadyExist() {
        Integer rawContent = 10;
        chapter.setContentsCount(rawContent);

        assertEquals(rawContent, chapter.getRawContentsCount((Context) any()));
    }
}
