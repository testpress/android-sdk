package in.testpress.ui;

import android.view.LayoutInflater;

import java.util.List;

import in.testpress.R;
import in.testpress.models.AccountActivity;
import in.testpress.util.FormatDate;
import in.testpress.util.SingleTypeAdapter;


public class AccountActivityAdapter extends SingleTypeAdapter<AccountActivity> {
    /**
     * @param inflater
     * @param items
     */
    public AccountActivityAdapter(final LayoutInflater inflater, final List<AccountActivity> items, int layout) {
        super(inflater, layout);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.title, R.id.lastUsed,
                R.id.location, R.id.ipAddress};
    }

    @Override
    protected void update(final int position, final AccountActivity item) {
        long lastUsedTime = FormatDate.getDate(item.getLastUsed(),
                "yyyy-MM-dd'T'HH:mm:ss", "UTC").getTime();
        setText(0, item.getUserAgent());
        setText(1, "Last Used : " + FormatDate.getAbbreviatedTimeSpan(lastUsedTime));
        setText(2, "Location: " + item.getLocation());
        setText(3, "IP Address " + item.getIpAddress());

        if(item.isCurrentDevice()) {
            setText(1, "Currently Using");
        }
    }
}
