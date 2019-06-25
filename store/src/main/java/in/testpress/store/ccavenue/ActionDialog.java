package in.testpress.store.ccavenue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import in.testpress.store.R;

public class ActionDialog extends DialogFragment {

    Communicator communicator;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        communicator = (Communicator) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Option");
        builder.setItems(R.array.tespress_ccavenue_selectAction, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    communicator.actionSelected("ResendOTP");
                }
                else if(which == 1){
                    communicator.actionSelected("EnterOTPManually");
                }

            }
        });

        builder.setNegativeButton(R.string.tespress_ccavenue_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                communicator.actionSelected("Cancel");
            }
        });

        /*builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });*/
        Dialog dialog = builder.create();
        return dialog;
    }
}
