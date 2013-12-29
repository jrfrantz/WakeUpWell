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
        System.out.println(mWeMoSDKContext);
        mWeMoSDKContext.addNotificationListener(this);

        
        toggleHeatButton = (Button) findViewById(R.id.button_heater_off);
        setUpToggle();
        
        mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN();
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
		System.out.println("notif " + event + notifUdn);
		if (event.equals(WeMoSDKContext.REFRESH_LIST)) {
			System.out.println("got a refresh");
			ArrayList<String> udns = mWeMoSDKContext.getListOfWeMoDevicesOnLAN();
			System.out.println(udns);
			for (String udn : udns) {
				WeMoDevice dev = mWeMoSDKContext.getWeMoDeviceByUDN(udn);
				System.out.println("Made dev " + dev);
				System.out.println(dev.getType() + " ? " + WeMoDevice.SWITCH);
				if (dev.getType().equals(WeMoDevice.SWITCH)) {
					System.out.println("hello");
					switchDev = dev;
					udnDev = udn;
					boolean on = switchDev.getState().equals(WeMoDevice.WEMO_DEVICE_ON);
					changeButtonState(on ? HEATER_ON : HEATER_OFF);
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
				changeButtonState(HEATER_ON);
			} else if (tempDev.getState().equals(WeMoDevice.WEMO_DEVICE_OFF)){
				changeButtonState(HEATER_OFF);
			}
		} else if (event.equals(WeMoSDKContext.REMOVE_DEVICE) && notifUdn.equals(udnDev)) {
			// device has gone dark
			udnDev = null;
			switchDev = null;
			changeButtonState(HEATER_DISABLED);
			mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN();
			Toast.makeText(getApplicationContext(), "Lost connection with switch", Toast.LENGTH_LONG).show();
			System.out.println("lost connection w switch");
		}
	}
	
	protected void onDestroy() {
		mWeMoSDKContext.stop();
		super.onDestroy();
	}

	/* B/C callback must run from UI thread */
	public void changeButtonState(String desired) {
		Runnable r = null;
		if (desired.equals(HEATER_ON)) {
			r = BUTTON_SAYS_ON;
		} else if (desired.equals(HEATER_OFF)) {
			r = BUTTON_SAYS_OFF;
		} else if (desired.equals(HEATER_DISABLED)) {
			r = BUTTON_NOT_CONNECTED;
		} else if (desired.equals(HEATER_WAITING_RESPONSE)) {
			r = BUTTON_WAITING;
		}
		runOnUiThread(r);
	}
	private final Runnable BUTTON_SAYS_ON = new Runnable() {
		public void run() {
			toggleHeatButton.setEnabled(true);
			toggleHeatButton.setText(HEATER_ON);
		}
	};
	private final Runnable BUTTON_SAYS_OFF = new Runnable() {
		public void run() {
			toggleHeatButton.setEnabled(true);
			toggleHeatButton.setText(HEATER_OFF);
		}
	};
	private final Runnable BUTTON_NOT_CONNECTED = new Runnable() {
		public void run() {
			toggleHeatButton.setEnabled(false);
			toggleHeatButton.setText(HEATER_DISABLED);
		}
	};
	private final Runnable BUTTON_WAITING = new Runnable() {
		public void run() {
			toggleHeatButton.setEnabled(false);
			toggleHeatButton.setText(HEATER_WAITING_RESPONSE);
		}
	};
}
