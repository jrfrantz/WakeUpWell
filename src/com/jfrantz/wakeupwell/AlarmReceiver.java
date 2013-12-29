package com.jfrantz.wakeupwell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("made it to the receiver");
        Toast.makeText(context, "hey there!", Toast.LENGTH_LONG).show();

        //Intent i = new Intent(context, com.jfrantz.wakeupwell.AwakenScreen.class);
        Intent i = new Intent();
        i.setClassName("com.jfrantz.wakeupwell", "com.jfrantz.wakeupwell.AwakenScreen");
        //activateHeater(i); // moving this logic to awaken
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
