package in.testpress.store.ccavenue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;


public class SmsReceiver extends BroadcastReceiver {

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {
            if(bundle!=null){
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    //if(senderNum.contains("SBIACS")){
                    String bankOtp = message.replaceFirst(".*?(\\d{6,8}).*", "$1");
                    //}else{
                        // else part.
                    //}

                    Intent in = new Intent("SmsMessage.intent.MAIN").putExtra("get_otp",bankOtp+"|"+senderNum);

                    context.sendBroadcast(in);


                    Log.v("SmsReceiver", "senderNum: " + senderNum + "; Banks OTP  " + bankOtp);


                    // Show Alert
                    /*int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "senderNum: "+ senderNum + ", OTP: " + bankOtp, duration);
                    toast.show();*/

                } // end for loop
            }
            //Toast.makeText(context, "SMS Recieved", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
