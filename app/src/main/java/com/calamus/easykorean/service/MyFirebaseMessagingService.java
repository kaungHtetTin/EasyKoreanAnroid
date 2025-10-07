package com.calamus.easykorean.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.calamus.easykorean.MainActivity;
import com.calamus.easykorean.app.Config;
import com.calamus.easykorean.app.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Ravi Tamada on 08/08/16.
 * www.androidhive.info
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FireBaseCM";

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "=== FCM MESSAGE RECEIVED ===");
        Log.d(TAG, "Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message Type: " + remoteMessage.getMessageType());
        Log.d(TAG, "Collapse Key: " + remoteMessage.getCollapseKey());
        Log.d(TAG, "TTL: " + remoteMessage.getTtl());
        Log.d(TAG, "Sent Time: " + remoteMessage.getSentTime());

        // Check notification payload
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Log.d(TAG, "Notification Title: " + notification.getTitle());
            Log.d(TAG, "Notification Body: " + notification.getBody());
            Log.d(TAG, "Notification Channel: " + notification.getChannelId());
            Log.d(TAG, "Notification Icon: " + notification.getIcon());
            Log.d(TAG, "Notification Color: " + notification.getColor());
            handleNotification(notification.getBody());
        } else {
            Log.d(TAG, "No notification payload");
        }

        // Check data payload
        if (!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Data payload size: " + data.size());
            for (Map.Entry<String, String> entry : data.entrySet()) {
                Log.d(TAG, "Data - " + entry.getKey() + ": " + entry.getValue());
            }
            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception processing data: " + e.getMessage(), e);
            }
        } else {
            Log.d(TAG, "No data payload");
        }

        Log.d(TAG, "=== FCM MESSAGE PROCESSING COMPLETE ===");
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
           // notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {

            String title = json.getString("title");
            String message = json.getString("message");
            boolean isBackground = json.getBoolean("is_background");
            String imageUrl = json.getString("image");
            String timestamp = json.getString("timestamp");
            String payloadStr=json.getString("payload");
            JSONObject payload = new JSONObject(payloadStr);
            String go=payload.getString("go");

            SharedPreferences sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor=sharedPreferences.edit();
            editor.putBoolean("notiRedMark",true);
            editor.apply();

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
               if(go.equals("2")){
                   Intent pushNotification = new Intent("MessageArrived");
                   pushNotification.putExtra("sender", title);
                   pushNotification.putExtra("message",message);
                   LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
               }
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message",go);
                resultIntent.putExtra("payload",payloadStr);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json NOti: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception Noti: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}