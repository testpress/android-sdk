package in.testpress.course.util;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadProgress;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;

import in.testpress.course.R;

public class DownloadTracker{
    public interface Listener {
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    private final HashMap<Uri, Download> downloads;
    private final DownloadIndex downloadIndex;

    @Nullable
    private StartDownloadDialogHelper startDownloadDialogHelper;

    public DownloadTracker(
            Context context, DataSource.Factory dataSourceFactory, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        Download download = downloads.get(uri.buildUpon().clearQuery().build());
        return download != null && download.state != Download.STATE_FAILED;
    }

    public float getDownloadPercentage(Uri uri) {
        Download download = downloads.get(uri.buildUpon().clearQuery().build());
        if (download != null) {
            return download.getPercentDownloaded();
        }
        return 0f;
    }

    public boolean isRemoved(Uri uri) {
        Download download = downloads.get(uri.buildUpon().clearQuery().build());
        return download == null;
    }

    public HashMap<Uri, Download> getDownloads() {
        return downloads;
    }

    public DownloadRequest getDownloadRequest(Uri uri) {
        Download download = downloads.get(uri.buildUpon().clearQuery().build());
        return download != null && download.state != Download.STATE_FAILED ? download.request : null;
    }

    public void removeDownload(Uri uri) {
        Download download = downloads.get(uri.buildUpon().clearQuery().build());
        if (download != null) {
            DownloadService.sendRemoveDownload(
                    context, TestpressDownloadService.class, download.request.id, /* foreground= */ false);
        }
        loadDownloads();
    }

    public void startDownload(
            FragmentManager fragmentManager,
            String name,
            Uri uri,
            String extension,
            RenderersFactory renderersFactory) {

        if (startDownloadDialogHelper != null) {
            startDownloadDialogHelper.release();
        }
        startDownloadDialogHelper =
                new StartDownloadDialogHelper(
                        fragmentManager, getDownloadHelper(uri, extension, renderersFactory), name);

    }

    public void toggleDownload(
            FragmentManager fragmentManager,
            String name,
            Uri uri,
            String extension,
            RenderersFactory renderersFactory) {
        Download download = downloads.get(uri.buildUpon().clearQuery().build());
        if (download != null) {
            removeDownload(uri);
        } else {
            startDownload(fragmentManager, name, uri, extension, renderersFactory);
        }
    }

    private void loadDownloads() {
        try  {
            DownloadCursor loadedDownloads = downloadIndex.getDownloads();
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri.buildUpon().clearQuery().build(), download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    private DownloadHelper getDownloadHelper(
            Uri uri, String extension, RenderersFactory renderersFactory) {
        int type = Util.inferContentType(uri, extension);
        switch (type) {
            case C.TYPE_DASH:
                return DownloadHelper.forDash(uri, dataSourceFactory, renderersFactory);
            case C.TYPE_SS:
                return DownloadHelper.forSmoothStreaming(uri, dataSourceFactory, renderersFactory);
            case C.TYPE_HLS:
                return DownloadHelper.forHls(uri, dataSourceFactory, renderersFactory);
            case C.TYPE_OTHER:
                return DownloadHelper.forProgressive(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {

        @Override
        public void onDownloadChanged(DownloadManager downloadManager, Download download) {
            downloads.put(download.request.uri.buildUpon().clearQuery().build(), download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
            downloads.remove(download.request.uri.buildUpon().clearQuery().build());
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }

    private final class StartDownloadDialogHelper
            implements DownloadHelper.Callback,
            DialogInterface.OnClickListener,
            DialogInterface.OnDismissListener {

        private final FragmentManager fragmentManager;
        private final DownloadHelper downloadHelper;
        private final String name;

        private TrackSelectionDialog trackSelectionDialog;
        private MappingTrackSelector.MappedTrackInfo mappedTrackInfo;

        public StartDownloadDialogHelper(
                FragmentManager fragmentManager, DownloadHelper downloadHelper, String name) {
            this.fragmentManager = fragmentManager;
            this.downloadHelper = downloadHelper;
            this.name = name;
            downloadHelper.prepare(this);
        }

        public void release() {
            downloadHelper.release();
            if (trackSelectionDialog != null) {
                trackSelectionDialog.dismiss();
            }
        }


        @Override
        public void onPrepared(DownloadHelper helper) {
            if (helper.getPeriodCount() == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.");
                startDownload();
                downloadHelper.release();
                return;
            }
            mappedTrackInfo = downloadHelper.getMappedTrackInfo(0);
            if (!TrackSelectionDialog.willHaveContent(mappedTrackInfo)) {
                Log.d(TAG, "No dialog content. Downloading entire stream.");
                startDownload();
                downloadHelper.release();
                return;
            }
            trackSelectionDialog =
                    TrackSelectionDialog.createForMappedTrackInfoAndParameters(
                            R.string.exo_download_description,
                            mappedTrackInfo,
                            DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS,
                            false,
                             true,
                             this,
                             this);
            trackSelectionDialog.show(fragmentManager, null);
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            Toast.makeText(
                    context.getApplicationContext(), R.string.download_start_error, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Failed to start download", e);
        }


        @Override
        public void onClick(DialogInterface dialog, int which) {
            for (int periodIndex = 0; periodIndex < downloadHelper.getPeriodCount(); periodIndex++) {
                downloadHelper.clearTrackSelections(periodIndex);
                for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                    if (!trackSelectionDialog.getIsDisabled(i)) {
                        downloadHelper.addTrackSelectionForSingleRenderer(
                                periodIndex,
                                i,
                                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS,
                                trackSelectionDialog.getOverrides(i));
                    }
                }
            }
            DownloadRequest downloadRequest = buildDownloadRequest();

            if (downloadRequest.streamKeys.isEmpty()) {
                return;
            }
            startDownload(downloadRequest);
        }


        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            trackSelectionDialog = null;
            downloadHelper.release();
        }


        private void startDownload() {
            startDownload(buildDownloadRequest());
        }

        private void startDownload(DownloadRequest downloadRequest) {
            DownloadService.sendAddDownload(
                    context, TestpressDownloadService.class, downloadRequest, /* foreground= */ false);
        }

        private DownloadRequest buildDownloadRequest() {
            return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name));
        }
    }
}
