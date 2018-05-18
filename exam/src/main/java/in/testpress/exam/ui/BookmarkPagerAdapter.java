package in.testpress.exam.ui;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.greenrobot.greendao.query.LazyList;

import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.Language;

class BookmarkPagerAdapter extends FragmentStatePagerAdapter {

    private FragmentActivity activity;
    private Language selectedLanguage;
    private boolean positionsModified;
    private LazyList<Bookmark> bookmarks;
    private String currentFolder;

    BookmarkPagerAdapter(FragmentActivity activity, String currentFolder) {
        super(activity.getSupportFragmentManager());
        this.currentFolder = currentFolder;
        this.activity = activity;
        bookmarks = Bookmark.getQueryBuilderToDisplay(activity, currentFolder).listLazy();
    }

    @Override
    public Fragment getItem(int position) {
        return BookmarksFragment.getInstance(bookmarks.get(position).getId(),
                selectedLanguage);
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

    void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    @Override
    public int getCount() {
        return bookmarks.size();
    }

    //This method will call when we call notifyDataSetChanged
    @Override
    public int getItemPosition(@NonNull Object object) {
        // Clear the fragments only if positions modified, just update the content otherwise.
        if (positionsModified) {
            return POSITION_NONE;
        }
        ((BookmarksFragment) object).updateContentObject();
        return super.getItemPosition(object);
    }

    void notifyDataSetChanged(boolean positionsModified) {
        this.positionsModified = positionsModified;
        bookmarks = Bookmark.getQueryBuilderToDisplay(activity, currentFolder).listLazy();
        super.notifyDataSetChanged();
        if (positionsModified) {
            this.positionsModified = false;
        }
    }

}
