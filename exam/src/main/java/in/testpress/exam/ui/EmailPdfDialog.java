package in.testpress.exam.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.network.RetrofitCall;
import in.testpress.util.UIUtils;

class EmailPdfDialog extends AlertDialog.Builder {

    /**
     * @param context the parent context
     * @param themeResId the resource ID of the theme against which to inflate this dialog
     * @param isExplanation true if needs to mail explanation also & urlFrag will be attempt's url
     *                      false if needs to mail questions only & urlFrag will be exam's url
     *
     * @param urlFrag url fragment of exam or attempt which pdf needs to mail
     */
    EmailPdfDialog(@NonNull final Context context, @StyleRes int themeResId,
                             final boolean isExplanation, final String urlFrag) {
        super(context, themeResId);
        setTitle(R.string.testpress_mail_pdf_confirm_title);
        setMessage(R.string.testpress_mail_pdf_confirm);
        setPositiveButton(R.string.testpress_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(context.getString(R.string.testpress_mail_pdf));
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                UIUtils.setIndeterminateDrawable(context, progressDialog, 4);
                progressDialog.show();
                RetrofitCall<Void> call;
                if (isExplanation) {
                    call = new TestpressExamApiClient(context).mailExplanationsPdf(urlFrag +
                            TestpressExamApiClient.MAIL_PDF_PATH);
                } else {
                    call = new TestpressExamApiClient(context).mailQuestionsPdf(urlFrag +
                            TestpressExamApiClient.MAIL_PDF_QUESTIONS_PATH);
                }
                call.enqueue(new TestpressCallback<Void>() {
                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                R.style.TestpressAppCompatAlertDialogStyle);
                        if (exception.isNetworkError()) {
                            builder.setTitle(R.string.testpress_network_error);
                            builder.setMessage(R.string.testpress_no_internet_try_again);
                        } else {
                            builder.setTitle(R.string.testpress_mail_pdf_error);
                            builder.setMessage(R.string.testpress_mail_pdf_error_description);
                        }
                        builder.setPositiveButton(R.string.testpress_ok, null);
                        builder.show();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                R.style.TestpressAppCompatAlertDialogStyle);
                        builder.setTitle(R.string.testpress_mail_pdf_complete);
                        builder.setMessage(R.string.testpress_mail_pdf_complete_description);
                        builder.setPositiveButton(R.string.testpress_ok, null);
                        builder.setCancelable(false);
                        builder.show();
                    }

                });
            }
        });
        setNegativeButton(R.string.testpress_cancel, null);
    }
}
