package jack.village;

/**
 * Created by jack on 02/02/2018.
 */


import android.content.Context;

public class GetTime {
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;

    public static String getTime(long time, Context context){

        if (time < 1000000000000L) {

            // If timestamp given in seconds, convert to millis
            time *= 1000;

        }

        long now = System.currentTimeMillis();

        if (time > now || time <= 0){

            return null;

        }

        // TODO: localize
        final long difference = now - time;

        if (difference < MINUTE){
            return "just now";
        } else if (difference < 2 * MINUTE) {
            return "a minute ago";
        } else if (difference < 50 * MINUTE){
            return (difference / MINUTE + " minutes ago");
        } else if (difference < 90 * MINUTE) {
            return "an hour ago";
        } else if (difference < 24 * HOUR){
            return (difference / HOUR + " hours ago");
        } else if (difference < 48 * HOUR){
            return "yesterday";
        } else {
            return difference / DAY + " days ago";
        }

    }
}