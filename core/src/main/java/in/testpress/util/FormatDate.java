package in.testpress.util;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormatDate {

    private static final String ABBREV_DAY = "d ago";
    private static final String ABBREV_HOUR = "h ago";
    private static final String ABBREV_MINUTE = "m ago";
    private static final String JUST_NOW = "Just now";

    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(String inputString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                Date date = simpleDateFormat.parse(inputString);
                simpleDateFormat = new SimpleDateFormat("dd MMM");
                String dateMonth = simpleDateFormat.format(date);
                simpleDateFormat = new SimpleDateFormat("yy  hh:mm a");
                String yearTime = simpleDateFormat.format(date)
                        .replace("AM", "am").replace("PM","pm");
                return dateMonth + " '" + yearTime;
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public static boolean compareDate(String dateString1, String dateString2, String inputFormat,
                                      String timezone) {

        Date date1 =  getDate(dateString1, inputFormat, timezone);
        Date date2 =  getDate(dateString2, inputFormat, timezone);
        return  date1 != null && date2 != null && date1.after(date2);
    }

    @SuppressLint("SimpleDateFormat")
    public static Date getDate(String inputString, String inputFormat, String timezone) {
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputFormat);
        if (timezone != null) {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                return date;
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public static String getAbbreviatedTimeSpan(long timeMillis) {
        long span = Math.max(System.currentTimeMillis() - timeMillis, 0);
        if (span >= DateUtils.WEEK_IN_MILLIS) {
            return DateUtils.getRelativeTimeSpanString(timeMillis).toString();
        }
        if (span >= DateUtils.DAY_IN_MILLIS) {
            return (span / DateUtils.DAY_IN_MILLIS) + ABBREV_DAY;
        }
        if (span >= DateUtils.HOUR_IN_MILLIS) {
            long hour = span / DateUtils.HOUR_IN_MILLIS;
            return hour + ABBREV_HOUR;
        }
        long min = span / DateUtils.MINUTE_IN_MILLIS;
        if (min == 0) {
            return JUST_NOW;
        }
        return min + ABBREV_MINUTE;
    }

}
