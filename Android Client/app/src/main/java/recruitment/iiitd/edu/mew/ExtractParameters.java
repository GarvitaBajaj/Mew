package recruitment.iiitd.edu.mew;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import recruitment.iiitd.edu.contextAwareness.ActivityRecognitionService;
import recruitment.iiitd.edu.contextAwareness.UserActivity;
import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.MobilityTrace;
import recruitment.iiitd.edu.utils.NetworkUtil;


public class ExtractParameters extends Service implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public AlarmManager alarmMgr;
	public PendingIntent alarmIntent;
	public Intent intentAlarm;
	public int code_for_state_change=1111;
	SensorManager mSensorManager;
	Sensor mAccelerometer,mGyroscope,mBarometer,mppg;
	float gyrpower,barpower,accpower,ppgpower;
	public static LocationManager locManager;
	static Double lat=200.0, lon=200.0;
	float value = 0.0f;
	int scale = -1;
	int level = -1;
	boolean isNetworkEnabled = false;
	public static WifiManager wm;
	WifiManager.WifiLock wifiLock;
	boolean isGPSEnabled = false;
    //	public static WifiManager.WifiLock lock;
    GoogleApiClient mGoogleApiClient;

    static PendingIntent pendingIntentActivity;
	PackageManager pm;
	public static SharedPreferences sharedpreferences;
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		return capitalize(manufacturer) + " " + model;
	}

	private static String capitalize(String str) {
		if (TextUtils.isEmpty(str)) {
			return str;
		}
		char[] arr = str.toCharArray();
		boolean capitalizeNext = true;
		String phrase = "";
		for (char c : arr) {
			if (capitalizeNext && Character.isLetter(c)) {
				phrase += Character.toUpperCase(c);
				capitalizeNext = false;
				continue;
			} else if (Character.isWhitespace(c)) {
				capitalizeNext = true;
			}
			phrase += c;
		}
		return phrase;
	}

	public static LocationListener locListener =new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(Constants.TAG,"GPS status changed...listener");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(Constants.TAG,"GPS provider enabled...listener");
			Location location=locManager.getLastKnownLocation(provider);
			if(location!=null) {
				lat = location.getLatitude();
				lon = location.getLongitude();
				Editor edit=sharedpreferences.edit();
				edit.putFloat("LATITUDE", Float.valueOf(lat.toString()));
				edit.putFloat("LONGITUDE", Float.valueOf(lon.toString()));
				edit.commit();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(Constants.TAG,"GPS provider disabled...listener");

		}

		@Override
		public void onLocationChanged(Location location) {

			if(location!=null) {
//				Log.d("mobility", "location changed");
				lat = location.getLatitude();
				lon = location.getLongitude();
				Editor edit=sharedpreferences.edit();
				edit.putFloat("LATITUDE", Float.valueOf(lat.toString()));
				edit.putFloat("LONGITUDE", Float.valueOf(lon.toString()));
				edit.commit();
//				Log.d("mobility", "Location changed to " + lat.toString() + "," + lon.toString());
			}else{
//				Log.d(Constants.TAG, "Location NUll");
			}
		};
	};

	BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			value=(float)level/scale;
			Editor edit=sharedpreferences.edit();
			edit.putFloat("BATTERY", value);
			edit.commit();
			if(value==0.05f){
				Map<String, Object> states = new HashMap<String, Object>();
				states.put("TYPE", Constants.MESSAGE_TYPE.LVNGPROVIDER.getValue());
				states.put("NODE", Constants.DEVICE_ID);
				RabbitMQConnections rabbitMQConnections=RabbitMQConnections.getInstance(arg0.getApplicationContext());
				rabbitMQConnections.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);
				//stop the service from sending more parameter updates to the server
				stopSelf();
			}

		}
	};


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		pm=this.getPackageManager();
		Log.d("MEW","Service Started");
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
		wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wm.startScan();
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "ExtractParameters");

		mGoogleApiClient.connect();
		SharedPreferences userPreferences= PreferenceManager.getDefaultSharedPreferences(this);
		//initialise location - GPS or mock locations
		{
//			boolean mockLocation;
			sharedpreferences = getApplicationContext().getSharedPreferences("StateValues", Context.MODE_PRIVATE);
//			try {
//				mockLocation = intent.getBooleanExtra("mockLocation", false);
//			} catch (Exception e) {
//				mockLocation = sharedpreferences.getBoolean("mockLocation", false);
//			}
			locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//			if (mockLocation) {
//				try {
//					if (locManager.getProvider("Test") == null) {
//						//todo if this part crashes here, run a normal thread to update location in the background
//						locManager.addTestProvider("Test", false, false, false, false, false, false, false, 0, 1);
//					}
//					locManager.setTestProviderEnabled("Test", true);
//					locManager.requestLocationUpdates("Test", 0, 0, locListener);
//				} catch (Exception e) {
//					Toast.makeText(this, "Please enable Mock Locations in Developer Options", Toast.LENGTH_SHORT).show();
//				}
//				MobilityTrace.getInstance(getApplicationContext());
//			} else {
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.TIME_BETWEEN_LOCATION_UPDATES, Constants.DISTANCE_BETWEEN_LOCATION_UPDATES, locListener);
				if (locManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
					locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.TIME_BETWEEN_LOCATION_UPDATES, Constants.DISTANCE_BETWEEN_LOCATION_UPDATES, locListener);
				isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				if (!isGPSEnabled) {
					//launch intent to start location services
					Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					gpsOptionsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(gpsOptionsIntent);
				}
				Location lastKnownLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (lastKnownLocation != null) {
					Editor edit = sharedpreferences.edit();
					edit.putFloat("LATITUDE", Float.valueOf(Double.valueOf(lastKnownLocation.getLatitude()).toString()));
					edit.putFloat("LONGITUDE", Float.valueOf(Double.valueOf(lastKnownLocation.getLongitude()).toString()));
					edit.commit();
				}
				isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//			}
		}
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		StringBuilder sb=new StringBuilder();
		if (wm.isWifiEnabled()){
			java.util.List<android.net.wifi.ScanResult> apList = wm.getScanResults();
			Set<ScanResult> setAPs= new HashSet<>(apList);
			for (ScanResult result : setAPs) {
//				String bssid=result.BSSID;
				sb.append(result.BSSID+",");
			}
		}else {sb.append("");}
		//initialise sensor powers
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//		Log.d("Shared acc value",String.valueOf(userPreferences.getBoolean("sen_accelerometer",true)));
		//check which sensors are present
		if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) || !userPreferences.getBoolean("sen_accelerometer",true)){
			//this device does not have a accelerometer - set accelerometer power to -1.0f
//			Log.d("Extract paramters","Accelerometer absent");
			accpower=-1.0f;
		}
		else{
			//initialise the accelerometer sensor to get its power
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
			accpower=mAccelerometer.getPower();
			mSensorManager.unregisterListener(this);
		}
		if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE) || !userPreferences.getBoolean("sen_gyroscope",true)){
			//this device does not have a GYROSCOPE - set GYROSCOPE power to -1.0f
//			Log.d("Extract paramters","Gyroscope absent");
			gyrpower=-1.0f;
		}
		else{
			//initialise the gyroscope sensor to get its power
			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
			gyrpower=mGyroscope.getPower();
			mSensorManager.unregisterListener(this);
		}
		if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER) || !userPreferences.getBoolean("sen_barometer",true)){
			//this device does not have a barometer - set barometer power to -1.0f
//			Log.d("Extract paramters","barometer absent");
			barpower=-1.0f;
		}
		else{
			mBarometer=mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
			mSensorManager.registerListener(this,mBarometer,SensorManager.SENSOR_DELAY_NORMAL);
			barpower=mBarometer.getPower();
			mSensorManager.unregisterListener(this);
		}
		if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE) || !userPreferences.getBoolean("sen_ppg",true)){
			//this device does not have a HEART_RATE - set HEART_RATE power to -1.0f
//			Log.d("Extract paramters","heart rate absent");
			ppgpower=-1.0f;
		}
		else{
			mppg=mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
			mSensorManager.registerListener(this,mppg,SensorManager.SENSOR_DELAY_NORMAL);
			ppgpower=mppg.getPower();
			mSensorManager.unregisterListener(this);
		}

		NetworkUtil networkUtil = new NetworkUtil();
		int linkSpeed= networkUtil.getSpeed(this);
		String cellIds=networkUtil.getCellIds(this);

//		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//		if((mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)||(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)!=null))
//		{
////			Log.d(Constants.TAG, " Gyroscope is present");
//			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
////			Log.d(Constants.TAG,mGyroscope.toString());
//			lg=new SensorEventListener() {
//
//				@Override
//				public void onSensorChanged(SensorEvent event) {
//
//				}
//
//				@Override
//				public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//				}
//			};
//			mSensorManager.registerListener(lg, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
//			gyrpower=mGyroscope.getPower();
//			mSensorManager.unregisterListener(lg);
//		}
//		else
//		{
//			Log.d(Constants.TAG,"Gyroscope absent");
//			gyrpower=-1.0f;
//		}

		SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("StateValues", Context.MODE_PRIVATE);
		Editor edit2=sharedpreferences.edit();
		edit2.putString("WiFiAPs",sb.toString());
		edit2.putString("MTOWERS",cellIds);
		switch(getDeviceName()){
			case "Asus Nexus 7":
			case "Motorola Nexus 6":
				edit2.putFloat("GPSPOWER", 416.67f);
				break;
			case "Motorola XT1033":
			case "Samsung GT-I8262": case "Samsung GT-B5330":
				edit2.putFloat("GPSPOWER",432.24f);
				break;
			case "Huawei Nexus 6P":
				//TODO check the power consumption- the number provided is incorrect
				edit2.putFloat("GPSPOWER", 436.23f);
				break;
			default:
				edit2.putFloat("GPSPOWER", 436.23f);
//				System.out.println(getDeviceName());
//				Toast.makeText(this,"Using the default GPS power for this device",Toast.LENGTH_LONG).show();
				break;
		}
		edit2.putInt("LINKSPEED", linkSpeed);
		edit2.putFloat("ACCPOWER", accpower);
		edit2.putFloat("GYRPOWER", gyrpower);
		edit2.putFloat("BARPOWER",barpower);
		edit2.putFloat("PPGPOWER",ppgpower);
		edit2.putFloat("LATITUDE", Float.valueOf(lat.toString()));
		edit2.putFloat("LONGITUDE", Float.valueOf(lon.toString()));
		edit2.putString("DEVICEID", Constants.DEVICE_ID);
		edit2.putFloat("MICPOWER",Float.valueOf("0.5"));
		edit2.putFloat("WIFIPOWER",Float.valueOf("0.7"));
		edit2.commit();

		Map<String, Object> states=new HashMap<String,Object>();
		states.put("TYPE", Constants.MESSAGE_TYPE.INFO.getValue());
		states.put("STATE", new JSONObject(sharedpreferences.getAll()));

		//App Memory Usage
		// look which parameters to fetch http://developer.android.com/reference/android/os/Debug.MemoryInfo.html and http://stackoverflow.com/questions/2298208/how-do-i-discover-memory-usage-of-my-application-in-android
		//Probably DalvikDirtyPss
//		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//		int id= android.os.Process.myPid();
//		int[] ids = {id};
//		Debug.MemoryInfo[] eme = activityManager.getProcessMemoryInfo(ids);
//		Debug.getMemoryInfo(eme[ 0 ]);
		RabbitMQConnections rabbitMQConnections = RabbitMQConnections.getInstance(this);
		rabbitMQConnections.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);

        //CPU Usage http://m2catalyst.com/tutorial-finding-cpu-usage-for-individual-android-apps/ via Waybackmachine
		alarmMgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
		intentAlarm = new Intent(this, recruitment.iiitd.edu.mew.RunningApplications.class);
		alarmIntent= PendingIntent.getService(this, code_for_state_change, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),Constants.TIME_BTW_RES_UPDATES,alarmIntent);

		return Service.START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognitionService.class );
        pendingIntentActivity = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 60000, pendingIntentActivity );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

	@SuppressWarnings("unchecked")
	public void onDestroy()
	{
		super.onDestroy();
		sendBroadcast(new Intent("YouWillNeverKillMe"));
		Map<String, Object> states=new HashMap<String,Object>();
		states.put("TYPE", Constants.MESSAGE_TYPE.LVNGLISTENER.getValue());
		states.put("NODE", Constants.DEVICE_ID);
		ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,pendingIntentActivity);
		//publish this message to RabbitMQ
		RabbitMQConnections publishResource= RabbitMQConnections.getInstance(this);
		publishResource.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);
        mGoogleApiClient.disconnect();
		alarmMgr.cancel(alarmIntent);
		this.unregisterReceiver(this.mBatInfoReceiver);
		locManager.removeUpdates(locListener);
//		Log.d(Constants.TAG, "Alarm cancelled & location updates removed");
//		lock.release();

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}