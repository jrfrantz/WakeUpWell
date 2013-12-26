package com.jfrantz.wakeupwell;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.belkin.wemo.localsdk.WeMoDevice;
import com.belkin.wemo.localsdk.WeMoSDKContext;
import com.belkin.wemo.localsdk.WeMoSDKContext.NotificationListener;

public class AwakenScreen extends Activity implements NotificationListener {
    Button toggleHeatButton;
    String udnDev;
    WeMoDevice switchDev;
    WeMoSDKContext mWeMoSDKContext;
    
    private static final String HEATER_ON = "Turn off heater";
    private static final String HEATER_OFF = "Turn on heater";
    private static final String HEATER_DISABLED = "Not connected to heater";
    private static final String HEATER_WAITING_RESPONSE = "Processing";

    //TODO: force screen on and locked, wait 5min and then text-to-speech
    // display (or just get) weather, twitter feed, fb
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awaken_screen);
        
        mWeMoSDKContext = new WeMoSDKContext(getApplicationContext());
        mWeMoSDKContext.addNotificationListener(this);
        mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN();
        
        toggleHeatButton = (Button) findViewById(R.id.button_heater_off);
        setUpToggle();
    }
    
    private void setUpToggle() {
    	toggleHeatButton.setText(HEATER_DISABLED);
    	toggleHeatButton.setEnabled(false);
    	
    	toggleHeatButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!toggleHeatButton.isEnabled()) {
					return;
				}
				
				if (udnDev == null || switchDev == null) {
					Toast.makeText(getApplicationContext(), "State error, no device", Toast.LENGTH_LONG).show();
					System.out.println("udnDev is " + udnDev +", switchDev is "+ switchDev);
				}
				
				if (toggleHeatButton.getText().equals(HEATER_ON)) {
					// heater is on, turn it off
					String newstate = WeMoDevice.WEMO_DEVICE_OFF;
					mWeMoSDKContext.setDeviceState(newstate, udnDev);
					toggleHeatButton.setText(HEATER_WAITING_RESPONSE);
				} else if (toggleHeatButton.getText().equals(HEATER_OFF)) {
					// heater is off, turn it on
					String newstate = WeMoDevice.WEMO_DEVICE_ON;
					mWeMoSDKContext.setDeviceState(newstate, udnDev);
					toggleHeatButton.setText(HEATER_WAITING_RESPONSE);
				}
			}
    	});
    }

	@Override
	public void onNotify(String event, String notifUdn) {
		if (event.equals(WeMoSDKContext.REFRESH_LIST)) {
			ArrayList<String> udns = mWeMoSDKContext.getListOfWeMoDevicesOnLAN();
			for (String udn : udns) {
				WeMoDevice dev = mWeMoSDKContext.getWeMoDeviceByUDN(udn);
				if (dev.getType().equals(WeMoDevice.SWITCH)) {
					switchDev = dev;
					udnDev = udn;
				}
			}
		} else if (event.equals(WeMoSDKContext.CHANGE_STATE) ||
				event.equals(WeMoSDKContext.SET_STATE)){
			
			if (!notifUdn.equals(udnDev)) {
				return;
			}
			// heater state just changed, adjust button
			WeMoDevice tempDev = mWeMoSDKContext.getWeMoDeviceByUDN(notifUdn);
			if (tempDev.getState().equals(WeMoDevice.WEMO_DEVICE_ON)) {
				// device is on, adjust button accordingly
				toggleHeatButton.setEnabled(true);
				toggleHeatButton.setText(HEATER_ON);
			} else if (tempDev.getState().equals(WeMoDevice.WEMO_DEVICE_OFF)){
				toggleHeatButton.setEnabled(true);
				toggleHeatButton.setText(HEATER_OFF);
			}
		} else if (event.equals(WeMoSDKContext.REMOVE_DEVICE) && notifUdn.equals(udnDev)) {
			// device has gone dark
			udnDev = null;
			switchDev = null;
			toggleHeatButton.setText(HEATER_DISABLED);
			toggleHeatButton.setEnabled(false);
			mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN();
			Toast.makeText(getApplicationContext(), "Lost connection with switch", Toast.LENGTH_LONG).show();
			System.out.println("lost connection w switch");
		}
	}
	
	protected void onDestroy() {
		mWeMoSDKContext.stop();
		super.onDestroy();
	}

}
