package com.jfrantz.wakeupwell;

import android.content.BroadcastReceiver;
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
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("made it to the receiver");
        Toast.makeText(context, "hey there!", Toast.LENGTH_LONG).show();

        activateHeater();
    }

    private void activateHeater() {

    }


}
