package com.malikas.malikaschat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Malik on 2017-12-07.
 */

// A service that extends FirebaseMessagingService.
// This is required if you want to do any message handling
// beyond receiving notifications on apps in the background.
// To receive notifications in foregrounded apps,
// to receive data payload, to send upstream messages,
// and so on, you must extend this service.
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // this one matches to the title property in payload of our firebase notification function
//        String notification_title = remoteMessage.getNotification().getTitle();
//        // this one matches to the body property in payload of our firebase notification function
//        String notification_message = remoteMessage.getNotification().getBody();
//        String click_action = remoteMessage.getNotification().getClickAction();

        String notification_title = remoteMessage.getData().get("title");
        String notification_message = remoteMessage.getData().get("body");
        String click_action = remoteMessage.getData().get("click_action");
        String from_user_id = remoteMessage.getData().get("from_user_Id");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(notification_title)
                        .setAutoCancel(true)
                        .setContentText(notification_message);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", from_user_id);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int notificationId = (int)System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

}
