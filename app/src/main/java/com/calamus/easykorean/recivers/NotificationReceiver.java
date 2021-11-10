package com.calamus.easykorean.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.calamus.easykorean.service.MusicService;

import static com.calamus.easykorean.ApplicationClass.ACTION_NEXT;
import static com.calamus.easykorean.ApplicationClass.ACTION_PLAY;
import static com.calamus.easykorean.ApplicationClass.ACTON_PREVIOUS;


public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName=intent.getAction();
        Intent serviceIntent=new Intent(context, MusicService.class);
        if(actionName!=null){
            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName","playPause");
                    context.startService(serviceIntent);
                    break;

                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName","next");
                    context.startService(serviceIntent);
                    break;

                case ACTON_PREVIOUS:
                    serviceIntent.putExtra("ActionName","previous");
                    context.startService(serviceIntent);
                    break;

            }
        }
    }
}
