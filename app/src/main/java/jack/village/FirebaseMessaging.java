package jack.village;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by jack on 02/02/2018.
 */

public class FirebaseMessaging extends com.google.firebase.messaging.FirebaseMessagingService {

    NotificationManager notificationManager;

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {

            //Calling method to generate notification
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

        //This method is generating push notification
        private void showNotification(String title, String messageBody) {
            //Sets activity to main activity
            Intent intent = new Intent(this, MainActivity.class);

            //Doesn't load new instance, instead closes all other activities on top
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //Creates a new pendingIntent, can only be used once
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            //Sets up the notification on the phone
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "village")
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

             notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        // The id of the channel.
        String id = "village";
        // The user-visible name of the channel.
        CharSequence name = "Village";
        // The user-visible description of the channel.
        String description = "Village Church";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel("village", "village", NotificationManager.IMPORTANCE_HIGH);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.BLUE);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(mChannel);

    }

}
