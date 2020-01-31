package in.testpress.course.ui.fragments.content_detail_fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.util.ViewUtils;

public class AttachmentContentFragment extends BaseContentDetailFragment {

    private LinearLayout attachmentContentLayout;
    private AttachmentDao attachmentDao;
    private TextView titleView;
    private LinearLayout titleLayout;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachmentDao = TestpressSDKDatabase.getAttachmentDao(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.attachment_content_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachmentContentLayout = (LinearLayout) view.findViewById(R.id.attachment_content_layout);
        titleView = (TextView) view.findViewById(R.id.title);
        titleLayout = (LinearLayout) view.findViewById(R.id.title_layout);


        if (content != null) {
            loadContent();
        }
    }

    @Override
    void loadContent() {
        titleView.setText(content.getTitle());
        titleLayout.setVisibility(View.VISIBLE);

        if (content.getRawAttachment() == null) {
            updateContent();
            return;
        }
        TextView description = (TextView) getView().findViewById(R.id.attachment_description);
        final Attachment attachment = content.getRawAttachment();
        if (attachment.getDescription() != null && !attachment.getDescription().isEmpty()) {
            description.setText(attachment.getDescription());
            description.setTypeface(TestpressSdk.getRubikRegularFont(getActivity()));
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }
        Button downloadButton = (Button) getView().findViewById(R.id.download_attachment);
        ViewUtils.setLeftDrawable(getActivity(), downloadButton, R.drawable.ic_file_download_18dp);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(attachment.getAttachmentUrl())));
            }
        });
        attachmentContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
        createContentAttempt();
    }

    @Override
    void onUpdateContent(Content fetchedContent) {
        Attachment attachment = fetchedContent.getRawAttachment();
        if (attachment != null) {
            attachmentDao.insertOrReplace(attachment);
            fetchedContent.setAttachmentId(attachment.getId());
        }
        contentDao.insertOrReplace(fetchedContent);
        content = fetchedContent;
    }

    @Override
    void onCreateContentAttempt(CourseAttempt courseAttempt) {}

    @Override
    void hideContents() {
        attachmentContentLayout.setVisibility(View.GONE);
    }
}
