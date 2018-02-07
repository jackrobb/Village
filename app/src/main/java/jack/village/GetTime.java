package jack.village;

/**
 * code sourced from https://www.youtube.com/watch?v=z-SjqtEhDo8&index=10&list=PLgqXWQqMyp4-NdaZDXCz7tVpN2LeqhV2G
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

        long currentTime = System.currentTimeMillis();

        if (time > currentTime || time <= 0){

            return null;

        }

        final long difference = currentTime - time;

        if (difference < MINUTE){
            return "just now";
        } else if (difference < 2 * MINUTE) {
            return "a minute ago";
        } else if (difference < 60 * MINUTE){
            return (difference / MINUTE + " minutes ago");
        } else if (difference < 24 * HOUR){
            return (difference / HOUR + " hours ago");
        } else {
            return difference / DAY + " days ago";
        }

    }
}