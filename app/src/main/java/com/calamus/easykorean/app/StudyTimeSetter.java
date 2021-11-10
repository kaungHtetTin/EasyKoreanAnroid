package com.calamus.easykorean.app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TimePicker;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.recivers.AlarmReceiver;
import java.util.ArrayList;
import java.util.Calendar;

public class StudyTimeSetter {

    private Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    FragmentActivity c;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    final int[] hour = new int[1];
    final int[] min = new int[1];
    ArrayList<Integer> alarmDay=new ArrayList<>();
    String currentUserId;


    public StudyTimeSetter(FragmentActivity c) {
        this.c = c;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","");
        editor=sharedPreferences.edit();
        alarmDay.add(Calendar.SUNDAY);
        alarmDay.add(Calendar.MONDAY);
        alarmDay.add(Calendar.TUESDAY);
        alarmDay.add(Calendar.WEDNESDAY);
        alarmDay.add(Calendar.THURSDAY);
        alarmDay.add(Calendar.FRIDAY);
        alarmDay.add(Calendar.SATURDAY);
    }

    public void setAlarm(){
        alarmManager=(AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(c, AlarmReceiver.class);
        pendingIntent= PendingIntent.getBroadcast(c,5241,intent,0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,pendingIntent);
        Intent intentClockAlarm=new Intent(AlarmClock.ACTION_SET_ALARM);
        intentClockAlarm.putExtra(AlarmClock.EXTRA_HOUR,hour[0]);
        intentClockAlarm.putExtra(AlarmClock.EXTRA_MINUTES, min[0]);
        intentClockAlarm.putExtra(AlarmClock.EXTRA_MESSAGE,Routing.timerMessage);
        intentClockAlarm.putExtra(AlarmClock.EXTRA_DAYS,alarmDay);
        if(intentClockAlarm.resolveActivity(c.getPackageManager())!=null)c.startActivity(intentClockAlarm);

    }

    public void cancelAlarm(){
        Intent intent=new Intent(c, AlarmReceiver.class);
        pendingIntent=PendingIntent.getBroadcast(c,5241,intent,0);
        if(alarmManager==null){
            alarmManager=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        }
        alarmManager.cancel(pendingIntent);
    }

    public void showTimePicker(){

        TimePicker timePicker;
        View v=c.getLayoutInflater().inflate(R.layout.custom_time_picker,null);
        v.setAnimation(AnimationUtils.loadAnimation(c,R.anim.transit_up));
        timePicker=v.findViewById(R.id.timePicker);
        AlertDialog.Builder builder=new AlertDialog.Builder(c);
        builder.setView(v);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour[0] =hourOfDay;
                min[0] =minute;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelAlarm();
                calendar= Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,hour[0]);
                calendar.set(Calendar.MINUTE,min[0]);
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);
                setAlarm();
                saveTimeOnServer(hour[0],min[0]);
                if(hour[0]>12){
                    editor.putString("studyTime",(hour[0]-12)+" : "+min[0]+"  PM");
                }else {
                    editor.putString("studyTime",hour[0]+" : "+min[0]+"  AM");
                }
                editor.putInt("studyHour",hour[0]);
                editor.putInt("studyMin",min[0]);
                editor.apply();

            }
        });

        builder.show();
    }

    private void saveTimeOnServer(int hour, int minute){
        String time=hour+":"+minute+":00";

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("Study Time", response);
                }
                @Override
                public void onError(String msg) {
                    Log.e("StudyTime Err:",msg);
                }
            }).url(Routing.SET_STUDY_TIME+"/"+currentUserId)
                    .field("studyTime",time);
            myHttp.runTask();
        }).start();
    }
}
