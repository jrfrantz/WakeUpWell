package com.jfrantz.wakeupwell;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private TimePicker timeP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        alarmMgr = (AlarmManager)this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this.getApplicationContext(), AlarmReceiver.class);

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        if (alarmIntent == null) {
            System.out.println("fuck");
        }

        System.out.println("Setting alarm");
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

        TextView tv = (TextView) findViewById(R.id.hello_tv);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        tv.setText("Next alarm is set for " + sdf.format(cal.getTime()));

        System.out.println("Set alarm for "+cal);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
    }
}
