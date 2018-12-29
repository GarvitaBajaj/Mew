package recruitment.iiitd.edu.sensing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;

import java.io.File;

import recruitment.iiitd.edu.model.DatabaseHelper;
import recruitment.iiitd.edu.model.QueryModel;
import recruitment.iiitd.edu.rabbitmq.SubscribeReceiver;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;
import recruitment.iiitd.edu.utils.Pair;

public class AlarmReceiver extends BroadcastReceiver {

	final String RECEIVED_ALARM="Alarm Broadcast";
//	String requesterID;

	@Override
	public void onReceive(Context context, Intent intent) {

//		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//		vibrator.vibrate(1000);		//let it vibrate for at least one second :P

		String queryNo = intent.getExtras().getString("queryNo");
		String filename=intent.getExtras().getString(Constants.FILENAME);
//		Log.d(Constants.TAG, RECEIVED_ALARM + " Query# " + queryNo + " Type " + intent.getExtras().get("intent"));

		DatabaseHelper db = new DatabaseHelper(context);
		QueryModel query = db.getQueryByNumber(queryNo);
		db.closeDb();

		if(query == null)
			return;

		if (intent.getExtras().get("intent").equals(Constants.START_DATA_REQUEST))// && query.getSensorName().equalsIgnoreCase("accelerometer"))
		{
			SubscribeReceiver.runningServices.getAndIncrement();
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+":Started collection for "+queryNo+"\n");
//			Log.d(Constants.TAG, "Starting service for " + queryNo+ "filename: "+filename );
			SensorReadings.processRequest(context, queryNo, Constants.START_DATA_REQUEST,query.getSensorName(),null);

		}
		else if (intent.getExtras().get("intent").equals(Constants.STOP_DATA_REQUEST))// && query.getSensorName().equalsIgnoreCase("accelerometer"))
		{
//			Log.e("TEST","STOP DATA REQUEST RECEIVED");
			SubscribeReceiver.runningServices.getAndDecrement();
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+":Stopped collection for "+queryNo+"\n");
			String fileName=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Mew/DataCollection/"+queryNo+"/"+query.getSensorName()+".csv";
//			Log.d(Constants.TAG, "Stopping service for " + queryNo + " system time " + System.currentTimeMillis() + " and query end time " + query.getEndTime()+ " filename:"+filename);
			SensorReadings.processRequest(context, queryNo, Constants.STOP_DATA_REQUEST,query.getSensorName(), fileName);
		}
	}

//	public Intent returnRelevantIntent(Context context, int queryId)
//	{
//		Intent i = null;
//		DatabaseHelper db = new DatabaseHelper(context);
//		QueryModel query = db.getQueryByID(queryId);
//		db.closeDb();
//		if (query.getSensorName().equalsIgnoreCase("accelerometer"))
//		{
////			i = new Intent(context, AccReadings.class);
////			i.putExtra("queryNo", queryNo);
////			i.putExtra("sensorName", "Accelerometer");
////			i.putExtra("fromTime", fromTime);
////			i.putExtra("toTime", toTime);
////			i.putExtra("requesterID",requesterID);
//
//
//		}
//
////		else if (sensorName.equalsIgnoreCase("gps"))
////		{
////			i = new Intent(context, GPSReadings.class);
////			i.putExtra("queryNo", queryNo);
////			i.putExtra("sensorName","GPS");
////			i.putExtra("fromTime", fromTime);
////			i.putExtra("toTime", toTime);
////			i.putExtra("requesterID",requesterID);
////		}
////
////		else if (sensorName.equalsIgnoreCase("microphone"))
////		{
////			i = new Intent(context, MicReadings.class);
////			i.putExtra("queryNo", queryNo);
////			i.putExtra("sensorName","Microphone");
////			i.putExtra("fromTime", fromTime);
////			i.putExtra("toTime", toTime);
////			i.putExtra("requesterID",requesterID);
////		}
////
////		else if (sensorName.equalsIgnoreCase("gyroscope"))
////		{
////			i = new Intent(context, GyrReadings.class);
////			i.putExtra("queryNo", queryNo);
////			i.putExtra("sensorName","Gyroscope");
////			i.putExtra("fromTime", fromTime);
////			i.putExtra("toTime", toTime);
////			i.putExtra("requesterID",requesterID);
////		}
//
//		return i;
//	}
}
