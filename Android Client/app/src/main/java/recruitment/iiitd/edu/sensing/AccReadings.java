//package recruitment.iiitd.edu.sensing;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Environment;
//import android.os.IBinder;
//import android.util.Log;
//
//import com.google.firebase.crash.FirebaseCrash;
//
//import org.json.JSONException;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//
//import recruitment.iiitd.edu.model.DatabaseHelper;
//import recruitment.iiitd.edu.model.QueryModel;
//import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
//import recruitment.iiitd.edu.utils.Constants;
//import recruitment.iiitd.edu.utils.LogTimer;
//import recruitment.iiitd.edu.utils.Pair;
//
//public class AccReadings extends Service implements SensorEventListener
//{
//	public static final String TAG = "AccReadings";
//
//	// push these to common interface
//
//	private static HashMap<String, Pair<BufferedWriter, File>> fileMap = new HashMap<>();
//	//triplets of query# and start/stop time and time type . Key is the time
//	private static TreeMap<Long, String> queryStartTime = new TreeMap<>();
//	private static TreeMap<Long, String> queryStopTime = new TreeMap<>();
//
//	private static List<String> queryList = new ArrayList<>();
//
//
//	private static final String QUERY_NO = "recruitment.iiitd.edu.sensing.extra.QUERY_NO";
//	private static final String JSON_OBJECT = "recruitment.iiitd.edu.sensing.extra.JSON_OBJECT";
//	private static final String REQUEST_TYPE = "recruitment.iiitd.edu.sensing.extra.REQUEST_TYPE";
//
//
//
//	SensorManager mSensorManager;
//	int myID;
//	//	File file = null;
////	BufferedWriter writer = null;
//	Sensor mAcc = null;
//	String queryNo,requesterID;//activity;
//	String sensorName;
//	private static Context appContext;
//	Intent queryIntent;
//
//	public static void processRequest(Context context, String queryNo, String requestType) {
//
//		QueryModel query = null;
//		try {
//			DatabaseHelper db = new DatabaseHelper(context);
//			query = db.getQueryByNumber(queryNo);
//			db.closeDb();
//		}catch (Exception e){
//			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
////			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////			FirebaseCrash.report(e);
//			return;
//
//		}
//		if(query == null)
//			return;
//		// type process
//		if(requestType.equalsIgnoreCase(Constants.PROCESS_DATA_REQUEST)){
//			Log.d(TAG, "Process Type request received for Query " + query.getId());
//			// add to queues
//			try {
//				Pair<BufferedWriter, File> ioPair = createFile(query.getQueryNo());
//				fileMap.put(query.getQueryNo(), ioPair);
//				queryStartTime.put(query.getStartTime(), query.getQueryNo());
//				queryStopTime.put(query.getEndTime(), query.getQueryNo());
//				Log.d(TAG, "Query Duration " + (query.getEndTime() - query.getStartTime()) + " Query Start Time - " + query.getStartTime() + ": End Time - " + query.getEndTime());
//				// update start and stop pending intents
//				Long startTime = queryStartTime.firstKey();
//				String startQueryNo = queryStartTime.get(startTime);
//
//				Intent startService = new Intent("dataRequest");
//				startService.putExtra("intent", Constants.START_DATA_REQUEST);
//				startService.putExtra("queryNo", startQueryNo);
//				startService.setAction(Constants.SENSING_ACTION);
//				PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, Constants.ACC_SERVICE_START_ID, startService, PendingIntent.FLAG_UPDATE_CURRENT);
//				Log.d(TAG, "Pending Start Intent- " + startTime + " Query# " + startQueryNo + " len(StartQueue) " + queryStartTime.size());
//
//				Long endTime = queryStopTime.firstKey();
//				String stopQueryNo = queryStopTime.get(endTime);
//
//				Intent stopService = new Intent("dataRequest");
//				stopService.putExtra("intent", Constants.STOP_DATA_REQUEST);
//				stopService.putExtra("queryNo", stopQueryNo);
//				stopService.setAction(Constants.SENSING_ACTION);
//				PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, Constants.ACC_SERVICE_STOP_ID, stopService, PendingIntent.FLAG_UPDATE_CURRENT);
//				Log.d(TAG, "Pending Stop Intent- " + endTime + " Query# " + stopQueryNo + " len(queryStopTime) " + queryStopTime.size());
//
//				AlarmManager startAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//				AlarmManager stopAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//				startAlarm.set(AlarmManager.RTC_WAKEUP, startTime, startPendingIntent);
//				stopAlarm.set(AlarmManager.RTC_WAKEUP, endTime, stopPendingIntent);
//			}catch (Exception e){
//				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
////				FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////				FirebaseCrash.report(e);
//				return;
//			}
//
//		}
//		else if(requestType.equalsIgnoreCase(Constants.START_DATA_REQUEST)){
//			// type start
//			// if not already running start service
//			// if running add next start pending intent
//			Log.d(TAG, "Start Type Request QueryID "+ query.getId() +"  Removing from Queue " + query.getStartTime());
//			queryStartTime.remove(query.getStartTime());
//
//			if(!queryStartTime.isEmpty()){
//
//				Long startTime = queryStartTime.firstKey();
//				String startQueryNo = queryStartTime.get(startTime);
//
//				Intent startService = new Intent("dataRequest");
//				startService.putExtra("intent", Constants.START_DATA_REQUEST);
//				startService.putExtra("queryNo", startQueryNo);
//				startService.setAction(Constants.SENSING_ACTION);
//				PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, Constants.ACC_SERVICE_START_ID, startService, PendingIntent.FLAG_UPDATE_CURRENT);
//				AlarmManager startAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//				startAlarm.set(AlarmManager.RTC_WAKEUP, startTime, startPendingIntent);
//				Log.d(TAG, "Pending Start Intent- " + startTime + " Query# " + startQueryNo + " len(StartQueue) " + queryStartTime.size());
//			}
//			Intent intent = new Intent(context, AccReadings.class);
//			context.startService(intent);
//		}
//
//		else if(requestType.equalsIgnoreCase(Constants.STOP_DATA_REQUEST)){
//			// type stop
//			// remove the files from queue
//			// publish the data
//			// update the stop intent
//			// if no more stop intents then exit
//			boolean collectionSucess = false;
//			Log.d(TAG, "Stop Type Request QueryID " + query.getId());
//			queryStopTime.remove(query.getEndTime());
//			Pair<BufferedWriter, File> ioPair = fileMap.remove(query.getQueryNo());;
//			try {
//				if(ioPair.getLeft() != null) {
//						ioPair.getLeft().close();
//						collectionSucess = true;
//						Log.d(TAG, "Accelerometer Data Collected For Query# " + query.getQueryNo());
//					}
//			} catch (Exception e) {
////				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//				//send message to the server that query could not be processed
//				DatabaseHelper db = new DatabaseHelper(context);
//				db.deleteQuery(query.getId());
//				db.closeDb();
//				Log.d(TAG, "Deleted From DB QueryID " + query.getId());
//				//todo send a message updating queries with some code
//				Map<String, Object> queryServiced = new HashMap<>();
//				queryServiced.put("TYPE", Constants.MESSAGE_TYPE.QUERYSERVICED.getValue());
//				queryServiced.put("NODE", Constants.DEVICE_ID);
//				queryServiced.put("QUERY", query.getQueryNo());
//				RabbitMQConnections publishResource = RabbitMQConnections.getInstance(context);
//				publishResource.addMessageToQueue(queryServiced, Constants.RESOURCE_ROUTING_KEY);
//			}
//
//			//code to send collected data to the requester
//			if(collectionSucess) {
//				try {
//					PublishSensorData.sendAsMessage(appContext, ioPair.getRight(), query.getQueryNo(), query.getSensorName(), query.getRoutingKey());
//					Map<String, Object> queryServiced = new HashMap<>();
//					queryServiced.put("TYPE", Constants.MESSAGE_TYPE.QUERYSERVICED.getValue());
//					queryServiced.put("NODE", Constants.DEVICE_ID);
//					queryServiced.put("QUERY", query.getQueryNo());
//					RabbitMQConnections publishResource = RabbitMQConnections.getInstance(context);
//					publishResource.addMessageToQueue(queryServiced, Constants.RESOURCE_ROUTING_KEY);
//
//					DatabaseHelper db = new DatabaseHelper(context);
//					db.deleteQuery(query.getId());
//					db.closeDb();
//					Log.d(TAG, "Deleted From DB QueryID " + query.getId());
//				} catch (JSONException e) {
//					LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
////					FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////					FirebaseCrash.report(e);
//				}
//			}
//
//			if(queryStopTime.isEmpty()){
//				Intent intent = new Intent(context, AccReadings.class);
//				context.stopService(intent);
//				return;
//			}
//			else {
//
//				Long endTime = queryStopTime.firstKey();
//				String stopQueryNo = queryStopTime.get(endTime);
////				Log.d(TAG, "updating Stop Pending Intent " + endTime + " for queryNo " + stopQueryNo);
//				Log.d(TAG, "Pending Stop Intent- "+ endTime + " Query# " + stopQueryNo +" len(queryStopTime) "+ queryStopTime.size() );
//				Intent stopService = new Intent("dataRequest");
//				stopService.putExtra("intent", Constants.STOP_DATA_REQUEST);
//				stopService.putExtra("queryNo", stopQueryNo);
//				stopService.setAction(Constants.SENSING_ACTION);
//				PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, Constants.ACC_SERVICE_STOP_ID, stopService, PendingIntent.FLAG_UPDATE_CURRENT);
//				AlarmManager stopAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//				stopAlarm.set(AlarmManager.RTC_WAKEUP, endTime, stopPendingIntent);
//			}
//		}
//	}
//
//	private static Pair<BufferedWriter,File> createFile(String queryNo){
//
//		File directory;
//		File file;
//		Pair<BufferedWriter, File> ioPair;
//		directory = new File(new File(Environment.getExternalStorageDirectory()+"/Mew/DataCollection/").getPath(), queryNo);
//		if (!directory.exists())
//		{
//			directory.mkdirs();
//		}
//		file = new File(directory,"acc.csv");
//		if(!file.exists())
//			try {
//				file.createNewFile();
//			} catch(FileNotFoundException e){
//				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + appContext.getClass().toString() + " : " + e.getMessage());
//				e.printStackTrace();
////				FirebaseCrash.logcat(Log.ERROR, "Exception caught", "File not found exception while creating the file");
////				FirebaseCrash.report(e);
//			}catch (IOException e1) {
//				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + appContext.getClass().toString() + " : " + e1.getMessage());
//				e1.printStackTrace();
////				FirebaseCrash.logcat(Log.ERROR, "Exception caught", "IO Exception while creating acc.csv");
////				FirebaseCrash.report(e1);
//			}
//
//		try
//		{
//			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
//			ioPair = new Pair<>(writer, file);
//			Log.d(TAG, "File created for queryNo " +queryNo);
//			return ioPair;
//		}
//		catch (IOException e)
//		{
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+appContext.getClass().toString()+" : "+e.getMessage());
//			Log.e(TAG, "File creation failed for QueryNo "+queryNo);
//			e.printStackTrace();
////			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////			FirebaseCrash.report(e);
//		}
//
//		return null;
//	}
//
//
//	@Override
//	public void onCreate()
//	{
//		super.onCreate();
//
//		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//		mSensorManager.registerListener(this, mAcc,SensorManager.SENSOR_DELAY_UI);
//
//		myID = Constants.SERVICE_START_ID;
//		Intent intent = new Intent(this, AccReadings.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//								| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//		Log.v(TAG, "Accelerometer Sensor Data Collection Service Created ");
//
//	}
//
//	@Override
//	public int onStartCommand(Intent intent_received, int flags, int startId)
//	{
//		//create file with the requested queryno
//		queryIntent=intent_received;
//		appContext=this.getApplicationContext();
//		return START_STICKY;
//	}
//
//	@Override
//	public void onAccuracyChanged(Sensor arg0, int arg1)
//	{
//
//	}
//
//	@Override
//	public void onSensorChanged(SensorEvent event)
//	{
//		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
//		{
//			return;
//		}
//
//		try
//		{
//			// add sensor data to all the active files
//			Iterator it = fileMap.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry pair = (Map.Entry)it.next();
//				Pair<BufferedWriter, File> ioPair = (Pair<BufferedWriter, File>) pair.getValue();
//				try {
//					BufferedWriter writer = ioPair.getLeft();
//					writer.write(System.currentTimeMillis() + "," + event.values[ 0 ] + ","
//										 + event.values[ 1 ] + "," + event.values[ 2 ] + "\n");
//				}catch (NullPointerException e){
////					Log.e(TAG,"Error while adding data to CSV Files !!!");
//				}
//			}
//
//		}
//		catch (IOException e)
//		{
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
//			Log.e(TAG,"Error while adding data to CSV Files !!!");
//			e.printStackTrace();
////			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////			FirebaseCrash.report(e);
//		}
//	}
//
//	@Override
//	public void onDestroy()
//	{
//		super.onDestroy();
//		Log.d(TAG, "Service Destroyed !!!");
//		mSensorManager.unregisterListener(this);
//		Map<String, Object> setServicing=new HashMap<>();
//		setServicing.put("TYPE",Constants.MESSAGE_TYPE.ENDSERVICING.getValue());
//		setServicing.put("NODE",Constants.DEVICE_ID);
//		RabbitMQConnections rabbitMQ= RabbitMQConnections.getInstance(this);
//		rabbitMQ.addMessageToQueue(setServicing,Constants.RESOURCE_ROUTING_KEY);
//
//	}
//
//	@Override
//	public IBinder onBind(Intent arg0)
//	{
//		return null;
//	}
//
//}
