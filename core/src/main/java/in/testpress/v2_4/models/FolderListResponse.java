package in.testpress.v2_4.models;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.BookmarkFolder;

public class FolderListResponse {

    private List<BookmarkFolder> folders = new ArrayList<BookmarkFolder>();

    public List<BookmarkFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<BookmarkFolder> folders) {
        this.folders = folders;
    }

}