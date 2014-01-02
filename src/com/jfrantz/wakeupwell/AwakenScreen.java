package com.jfrantz.wakeupwell;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.belkin.wemo.localsdk.WeMoDevice;
import com.belkin.wemo.localsdk.WeMoSDKContext;
import com.belkin.wemo.localsdk.WeMoSDKContext.NotificationListener;
import com.example.androidwwo.LocalWeather;
import com.example.androidwwo.LocationSearch;
import com.example.androidwwo.WeatherRetriever;

public class AwakenScreen extends Activity implements NotificationListener, OnInitListener {
    Button toggleHeatButton;
    String udnDev;
    WeMoDevice switchDev;
    WeMoSDKContext mWeMoSDKContext;
    CountDownTimer cdTimer;
    TextView tv;
	private boolean connectWhenHeaterAvailable; // true when initiated
	private TextToSpeech mTTS;
	
	/* For grabbing weather data*/
	public LocalWeather.Data weather;
    public LocationSearch.Data loc;
	
    private static final String HEATER_ON = "Turn off heater";
    private static final String HEATER_OFF = "Turn on heater";
    private static final String HEATER_DISABLED = "Not connected to heater";
    private static final String HEATER_WAITING_RESPONSE = "Processing";
    private static final int HOW_EARLY = 1*60*1000; // turn on heater this long before voice turns on.

    //TODO: force screen on and locked, wait 5min and then text-to-speech
    // display (or just get) weather, twitter feed, fb
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awaken_screen);
        
        mWeMoSDKContext = new WeMoSDKContext(getApplicationContext());
        System.out.println(mWeMoSDKContext);
        mWeMoSDKContext.addNotificationListener(this);

        tv = (TextView) findViewById(R.id.tv_awake_hello);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        toggleHeatButton = (Button) findViewById(R.id.button_heater_off);
        setUpToggle();
        connectWhenHeaterAvailable = true;
        mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN();
        
        cdTimer = new CountDownTimer(HOW_EARLY, HOW_EARLY){
			@Override
			public void onFinish() {
				// turn on the screen, acquire wake lock, 
				// say "hello"
				// get weather x twitter
			}
			@Override
			public void onTick(long millisUntilFinished) {}
        }.start();
        
        mTTS = new TextToSpeech(this, this);
        System.out.println("Made tts");

        
        
        // get weather Asynchronously
        Log.d("async", "about to start");
        new AsyncTask<Object, Object, Object>() {
        	String txt;
			@Override
			protected Object doInBackground(Object... params) {
				txt = new WeatherRetriever("33179").getWeather();
				return null;
			}
			protected void onPostExecute(Object result) {
				tv.append(txt);
			}
			
        	
        }.execute();
        /*new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				Log.d("async", "got to asynctask");
				MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
					@Override
					public void gotLocation(Location location) {
						Boolean Done = false;
                        synchronized (Done) {
                                if(!Done) {
                                        String q = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());

                                        //get weather
                                        LocalWeather lw = new LocalWeather(true);
                                        Log.d("async", "lw = "+lw);
                                        String query = (lw.new Params(lw.key)).setQ(q).getQueryString(LocalWeather.Params.class);
                                        Log.d("async", "starting api query");
                                        weather = lw.callAPI(query);
                                        Log.d("async", weather.toString());
                                        
                                        //get location
                                        Log.d("async", "making a LocationSearch");
                                        LocationSearch ls = new LocationSearch(true);
                                        Log.d("async", "ls " + ls);
                                        query = (ls.new Params(ls.key)).setQuery(q).getQueryString(LocationSearch.Params.class);
                                        loc = ls.callAPI(query);
                                        Log.d("async", "loc " + loc);
                                        
                                        Done = true;
                                }
                        }
					}
				};
				Log.d("async", locationResult.toString());
				return locationResult;
			}
        	
        }.execute();*/
        Log.d("async", "began execution");
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
			tv.append(udns.toString()+"\n");
			System.out.println(udns);
			if (udns.isEmpty() && connectWhenHeaterAvailable) {
				// give it another shot
				mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN();
				return;
			}
			for (String udn : udns) {
				WeMoDevice dev = mWeMoSDKContext.getWeMoDeviceByUDN(udn);
				System.out.println("Made dev " + dev);
				System.out.println(dev.getType() + " ? " + WeMoDevice.SWITCH);
				if (dev.getType().equals(WeMoDevice.SWITCH)) {
					System.out.println("hello");
					switchDev = dev;
					udnDev = udn;
					if (connectWhenHeaterAvailable) {
						connectWhenHeaterAvailable = false;
						String newstate = WeMoDevice.WEMO_DEVICE_ON;
						mWeMoSDKContext.setDeviceState(newstate, udnDev);
					}
					boolean on = switchDev.getState().equals(WeMoDevice.WEMO_DEVICE_ON);
					changeButtonState(on ? HEATER_ON : HEATER_OFF);
					break; // in case there are more switches
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
		super.onDestroy();
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		mWeMoSDKContext.stop();
		

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

	/* TTS has finished loading */
	public void onInit(int status) {
		if (status == TextToSpeech.ERROR) {
			return;
		}
        mTTS.setLanguage(Locale.US);
        System.out.println("Set Locale");
		mTTS.speak("Good morning.", TextToSpeech.QUEUE_FLUSH, null);
	}
}
