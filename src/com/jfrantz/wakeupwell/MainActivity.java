package com.jfrantz.wakeupwell;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.belkin.wemo.localsdk.WeMoSDKContext;

public class MainActivity extends Activity {

    public static int MINUTES_BEFORE = 5;
	private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private TimePicker timeP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        System.out.println(android.os.Build.VERSION.SDK_INT);

        alarmMgr = (AlarmManager)this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this.getApplicationContext(), AlarmReceiver.class);

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        if (alarmIntent == null) {
            System.out.println("fuck");
        }

        System.out.println("Setting alarm");
        /*alarmMgr.set(AlarmManager.ELAPSED_REALTIME,
                8*1000*1000, //10 seconds
                alarmIntent);
        System.out.println("set alarm");*/
    }

    public void alarmSet(View v) {
        Toast.makeText(this, "Alarm button pressed", Toast.LENGTH_SHORT).show();
        timeP = (TimePicker) findViewById(R.id.alarm_time);
        int h = timeP.getCurrentHour();
        int m = timeP.getCurrentMinute();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);
        
        cal.add(Calendar.MINUTE, -1*MINUTES_BEFORE);

        TextView tv = (TextView) findViewById(R.id.hello_tv);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        tv.setText("Next alarm is set for " + sdf.format(cal.getTime()));

        System.out.println("Set alarm for "+cal);
        System.out.println("AKA "+sdf.format(cal.getTime()));
        alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
    }
    public void alarmCancel(View v) {
    	Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show();
    	if (alarmMgr == null) {
    		alarmMgr = (AlarmManager) this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    	}
    	if (alarmIntent == null) {
            Intent intent = new Intent(this.getApplicationContext(), AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
    	}
    	alarmMgr.cancel(alarmIntent);
    	
    	TextView tv = (TextView) findViewById(R.id.hello_tv);
    	tv.setText(R.string.no_alarms_set);
    }
}