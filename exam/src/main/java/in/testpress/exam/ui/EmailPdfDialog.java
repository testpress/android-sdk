package in.testpress.exam.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;

import in.testpress.exam.R;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.util.SafeAsyncTask;
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
                new SafeAsyncTask<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (isExplanation) {
                            new TestpressExamApiClient(context).mailExplanationsPdf(urlFrag +
                                    TestpressExamApiClient.MAIL_PDF_PATH);
                        } else {
                            new TestpressExamApiClient(context).mailQuestionsPdf(urlFrag +
                                    TestpressExamApiClient.MAIL_PDF_QUESTIONS_PATH);
                        }
                        return null;
                    }

                    @Override
                    protected void onException(Exception e) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                R.style.TestpressAppCompatAlertDialogStyle);
                        builder.setTitle(R.string.testpress_mail_pdf_error);
                        builder.setMessage(R.string.testpress_mail_pdf_error_description);
                        builder.setPositiveButton(R.string.testpress_ok, null);
                        builder.show();
                    }

                    @Override
                    protected void onSuccess(Void arg) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                R.style.TestpressAppCompatAlertDialogStyle);
                        builder.setTitle(R.string.testpress_mail_pdf_complete);
                        builder.setMessage(R.string.testpress_mail_pdf_complete_description);
                        builder.setPositiveButton(R.string.testpress_ok, null);
                        builder.setCancelable(false);
                        builder.show();
                    }

                }.execute();
            }
        });
        setNegativeButton(R.string.testpress_cancel, null);
    }
}
