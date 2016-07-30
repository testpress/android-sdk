package in.testpress.exam.ui;

import android.view.LayoutInflater;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.util.SingleTypeAdapter;

/**
 * List adapter that colors rows in alternating colors
 *
 * @param <V>
 */
public abstract class AlternatingColorListAdapter<V> extends SingleTypeAdapter<V> {

    private final int primaryResource;
    private final int secondaryResource;

    /**
     * Create adapter with alternating row colors
     *
     * @param layoutId
     * @param inflater
     * @param items
     */
    public AlternatingColorListAdapter(final int layoutId, final LayoutInflater inflater,
                                       final List<V> items) {
        super(inflater, layoutId);
        primaryResource = R.color.testpress_pager_background;
        secondaryResource = R.color.testpress_pager_background_alternate;
        setItems(items);
    }

    @Override
    protected void update(final int position, final V item) {
        if (position % 2 != 0)
            updater.view.setBackgroundResource(primaryResource);
        else
            updater.view.setBackgroundResource(secondaryResource);
    }
}
