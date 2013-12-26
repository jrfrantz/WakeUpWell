package com.jfrantz.wakeupwell;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AwakenScreen extends Activity {
    Button toggleHeatButton;
    String udn;
    //WeMoDevice switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awaken_screen);

        toggleHeatButton = (Button) findViewById(R.id.button_heater_off);
    }

}
