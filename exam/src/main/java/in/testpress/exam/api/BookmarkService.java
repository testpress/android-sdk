package in.testpress.exam.api;

import java.util.Map;

import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.network.RetrofitCall;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.BookmarksListResponse;
import in.testpress.v2_4.models.FolderListResponse;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

import static in.testpress.exam.api.TestpressExamApiClient.BOOKMARKS_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;

public interface BookmarkService {

    @GET
    RetrofitCall<ApiResponse<FolderListResponse>> getBookmarkFolders(@Url String bookmarkFolderUrl);

    @PUT(BOOKMARK_FOLDERS_PATH + "{folder_id}/")
    RetrofitCall<BookmarkFolder> updateFolder(
            @Path(value = "folder_id", encoded = true) Long folderId,
            @Body Map<String, String> options);

    @DELETE(BOOKMARK_FOLDERS_PATH + "{folder_id}/")
    RetrofitCall<Void> deleteFolder(@Path(value = "folder_id", encoded = true) Long folderId);

    @POST(BOOKMARKS_PATH)
    RetrofitCall<Bookmark> bookmark(@Body Map<String, Object> options);

    @PUT(BOOKMARKS_PATH + "{bookmark_id}/")
    RetrofitCall<Bookmark> updateBookmark(
            @Path(value = "bookmark_id", encoded = true) Long bookmarkId,
            @Body Map<String, Object> options);

    @DELETE(BOOKMARKS_PATH + "{bookmark_id}/")
    RetrofitCall<Void> deleteBookmark(
            @Path(value = "bookmark_id", encoded = true) Long bookmarkId);

    @GET(BOOKMARKS_PATH)
    RetrofitCall<ApiResponse<BookmarksListResponse>> getBookmarks(
            @QueryMap Map<String, Object> options);

}


