package in.testpress.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormatDate {

    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(String inputString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                Date date = simpleDateFormat.parse(inputString);
                simpleDateFormat = new SimpleDateFormat("dd MMM");
                String dateMonth = simpleDateFormat.format(date);
                simpleDateFormat = new SimpleDateFormat("yy  HH:mm a");
                String yearTime = simpleDateFormat.format(date)
                        .replace("AM", "am").replace("PM","pm");
                return dateMonth + " '" + yearTime;
            }
        } catch (ParseException e) {
        }
        return null;
    }

}
