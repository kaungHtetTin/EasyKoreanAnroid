package com.calamus.easykorean.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.calamus.easykorean.MainActivity;
import com.calamus.easykorean.app.NotificationUtils;

public class AlarmReceiver extends BroadcastReceiver {

    SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences=context.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        String name=sharedPreferences.getString("Username","Guy");
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("message","splash");
        showNotificationMessage(context, System.currentTimeMillis()+"", resultIntent,name);
    }

    private void showNotificationMessage(Context context, String timeStamp, Intent intent,String name) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage("Hello "+name+"!", "It's time to study Korean language", timeStamp, intent);
    }
}
