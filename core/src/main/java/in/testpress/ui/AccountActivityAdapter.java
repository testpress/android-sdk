package in.testpress.ui;

import android.text.Html;
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
        return new int[]{R.id.lastUsed,
                R.id.deviceName, R.id.ipAddress};
    }

    @Override
    protected void update(final int position, final AccountActivity item) {
        long lastUsedTime = FormatDate.getDate(item.getLastUsed(),
                "yyyy-MM-dd'T'HH:mm:ss", "UTC").getTime();
        setText(0, Html.fromHtml("Last accessed on <b>" + FormatDate.getAbbreviatedTimeSpan(lastUsedTime) + "</b>"));
        setText(1, item.getUserAgent());
        setText(2, item.getIpAddress());

        if(item.isCurrentDevice()) {
            setText(0, "Currently Using");
        }
    }
}
