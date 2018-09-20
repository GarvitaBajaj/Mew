package recruitment.iiitd.edu.mew;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.util.Log;

//import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;
import recruitment.iiitd.edu.utils.NetworkUtil;

public class RunningApplications extends IntentService {

	public RunningApplications() {
		super("Running Applications Change");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {
		boolean GPSenabled=false;
		
		LocationManager lm=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE );
		if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			GPSenabled=true;

		SharedPreferences sharedpreferences = null;
		ActivityManager actvityManager = (ActivityManager)
				this.getSystemService( ACTIVITY_SERVICE );
		List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
		System.out.println("PROCESSES RUNNING: "+String.valueOf(procInfos.size()));
		try{
			sharedpreferences= ExtractParameters.sharedpreferences;
			Editor edit=sharedpreferences.edit();
			edit.putBoolean("GPSRunning", GPSenabled);
			edit.putInt("ACTIVITIES",procInfos.size());
			NetworkUtil networkUtil = new NetworkUtil();
			edit.putFloat("LINKSPEED", networkUtil.getSpeed(this));
			edit.commit();
		}catch (Exception e){
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//			FirebaseCrash.report(e);
		}
		if(sharedpreferences != null) {
			Map<String, Object> states = new HashMap<String, Object>();
			states.put("TYPE", Constants.MESSAGE_TYPE.INFO.getValue());
			states.put("STATE", new JSONObject(sharedpreferences.getAll()));
			RabbitMQConnections publishResource= RabbitMQConnections.getInstance(this);
			publishResource.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);
		}
		else{
			Log.e("RunningApplications","Shared Preference is NULL");
		}


		stopSelf();
	}
}
