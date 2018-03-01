package in.testpress.store.ui;

import android.view.LayoutInflater;

import java.util.List;

import in.testpress.store.R;
import in.testpress.store.models.Notes;
import in.testpress.util.SingleTypeAdapter;

public class NotesListAdapter extends SingleTypeAdapter<Notes> {

    public NotesListAdapter(final LayoutInflater inflater, final List<Notes> items, int layout) {
        super(inflater, layout);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.description };
    }

    @Override
    protected void update(final int position, final Notes item) {
        setText(0, item.getTitle());
        if (item.getDescription().isEmpty()) {
            setText(1, R.string.testpress_no_description);
        } else {
            setText(1, item.getDescription());
        }
    }
}
