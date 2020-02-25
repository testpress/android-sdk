package in.testpress.exam.ui.loaders;


import android.content.Context;
import android.util.Log;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.ui.TestFragment;
import in.testpress.util.ThrowableLoader;

public class AttemptItemsLoader extends ThrowableLoader<List<AttemptItem>> {
    private TestFragment fragment;
    private boolean fetchSinglePageOnly;

    public AttemptItemsLoader(Context context, TestFragment fragment, boolean fetchSinglePageOnly) {
        super(context, null);
        this.fragment = fragment;
        this.fetchSinglePageOnly = fetchSinglePageOnly;
    }

    @Override
    public List<AttemptItem> loadData() throws TestpressException {
        if (fetchSinglePageOnly) {
            fragment.questionsResourcePager.next();
            fragment.totalQuestions = fragment.questionsResourcePager.getResponse().getCount();
        } else {
            do {
                fragment.questionsResourcePager.next();
            } while (fragment.questionsResourcePager.hasNext());
        }
        return fragment.questionsResourcePager.getResources();
    }
}