package jack.village;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by jack on 02/02/2018.
 */

public class FirebaseMessaging extends com.google.firebase.messaging.FirebaseMessagingService {

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {

            //Calling method to generate notification
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

        //This method is generating push notification
        private void sendNotification(String title, String messageBody) {
            //Sets activity to main activity
            Intent intent = new Intent(this, MainActivity.class);

            //Doesn't load new instance, instead closes all other activities on top
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //Creates a new pendingIntent, can only be used once
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            //Sets up the notification on the phone
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "Village")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            notificationManager.notify(0, notificationBuilder.build());
        }
}
