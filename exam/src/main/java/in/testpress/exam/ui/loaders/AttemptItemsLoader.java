package in.testpress.exam.ui.loaders;


import android.content.Context;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.ui.TestFragment;
import in.testpress.util.ThrowableLoader;

public class AttemptItemsLoader extends ThrowableLoader<List<AttemptItem>> {
    private TestFragment fragment;

    public AttemptItemsLoader(Context context, TestFragment fragment) {
        super(context, null);
        this.fragment = fragment;
    }

    @Override
    public List<AttemptItem> loadData() throws TestpressException {
        do {
            fragment.questionsResourcePager.next();
        } while (fragment.questionsResourcePager.hasNext());
        return fragment.questionsResourcePager.getResources();
    }
}