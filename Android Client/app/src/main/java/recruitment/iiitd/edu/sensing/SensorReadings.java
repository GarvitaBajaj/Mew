package recruitment.iiitd.edu.sensing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import recruitment.iiitd.edu.implementations.AccelerometerRecord;
//import recruitment.iiitd.edu.implementations.LocationRecord;
//import recruitment.iiitd.edu.implementations.MicRecord;
//import recruitment.iiitd.edu.implementations.NetworkStateRecord;
import recruitment.iiitd.edu.implementations.WifiStateRecord;
import recruitment.iiitd.edu.implementations.MicRecord;
import recruitment.iiitd.edu.implementations.WifiStateRecord;
import recruitment.iiitd.edu.model.DatabaseHelper;
import recruitment.iiitd.edu.model.QueryModel;
import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;
import recruitment.iiitd.edu.utils.Pair;

public class SensorReadings extends Service {
    public static final String TAG = "SensorReadings";

    // push these to common interface

    private static HashMap<String, Pair<BufferedWriter, File>> fileMap = new HashMap<>();
    //triplets of query# and start/stop time and time type . Key is the time
    private static TreeMap<Long, String> queryStartTime = new TreeMap<>();
    private static TreeMap<Long, String> queryStopTime = new TreeMap<>();


    static AccelerometerRecord accelerometerRecord;
    static WifiStateRecord wifiStateRecord;
    static MicRecord micRecord;

    int myID;
    String queryNo;
    String sensorName;
    Intent queryIntent;

    public static void processRequest(Context context, String queryNo, String requestType, String sensorName,String fileSent) {
        QueryModel query = null;

        try {
            DatabaseHelper db = new DatabaseHelper(context);
            query = db.getQueryByNumber(queryNo);
            db.closeDb();
        } catch (Exception e) {
            LogTimer.error(e.getMessage() + " for query# " + queryNo);
            e.printStackTrace();
            return;
        }
        if (query == null)
            return;
        // type process
        if (requestType.equalsIgnoreCase(Constants.PROCESS_DATA_REQUEST)) {
//            Log.d(TAG, "Process Type request received for Query " + query.getId());
            // add to queues
            try {
                Pair<BufferedWriter, File> ioPair = createFile(query.getQueryNo(), sensorName);
                fileMap.put(query.getQueryNo(), ioPair);
                queryStartTime.put(query.getStartTime(), query.getQueryNo());
                queryStopTime.put(query.getEndTime(), query.getQueryNo());
//                Log.d(TAG, "Query Duration " + (query.getEndTime() - query.getStartTime()) + "Query Start Time - " + query.getStartTime() + ": End Time - " + query.getEndTime());
                // update start and stop pending intents
                Long startTime = queryStartTime.firstKey();
                String startQueryNo = queryStartTime.get(startTime);
//                filename=ioPair.getRight().getAbsolutePath(); //the filename can't be a static variable; its value will keep updating in case of multiple queries sent

                Intent startService = new Intent("dataRequest");
                startService.putExtra("intent", Constants.START_DATA_REQUEST);
                startService.putExtra(Constants.QUERY_NO, startQueryNo);
                startService.putExtra("dataReqd", sensorName);
                startService.putExtra(Constants.FILENAME,ioPair.getRight().getAbsolutePath());
               startService.setAction(Constants.SENSING_ACTION);
                PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, Constants.SENSOR_SERVICE_START_ID, startService, PendingIntent.FLAG_UPDATE_CURRENT);
                Log.d(TAG, "Pending Start Intent- " + startTime + " Query# " + startQueryNo + " len(StartQueue) " + queryStartTime.size());

                Long endTime = queryStopTime.firstKey();
                String stopQueryNo = queryStopTime.get(endTime);

                Intent stopService = new Intent("dataRequest");
                stopService.putExtra("intent", Constants.STOP_DATA_REQUEST);
                stopService.putExtra(Constants.QUERY_NO, stopQueryNo);
                stopService.putExtra(Constants.FILENAME,ioPair.getRight().getAbsolutePath());
                stopService.setAction(Constants.SENSING_ACTION);
                PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, Constants.SENSOR_SERVICE_STOP_ID, stopService, PendingIntent.FLAG_UPDATE_CURRENT);
                Log.d(TAG, "Pending Stop Intent- " + endTime + " Query# " + stopQueryNo + " len(queryStopTime) " + queryStopTime.size());

                AlarmManager startAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                AlarmManager stopAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                startAlarm.set(AlarmManager.RTC_WAKEUP, startTime, startPendingIntent);
                stopAlarm.set(AlarmManager.RTC_WAKEUP, endTime, stopPendingIntent);
            } catch (Exception e) {
                LogTimer.error(e.getMessage() + " Query Processing Error for Query# " + queryNo);
                e.printStackTrace();
                return;
            }

        } else if (requestType.equalsIgnoreCase(Constants.START_DATA_REQUEST)) {
            // type start
            // if not already running start service
            // if running add next start pending intent
//            Log.d(TAG, "Start Type Request QueryID " + query.getId() + "  Removing from Queue " + query.getStartTime());
            queryStartTime.remove(query.getStartTime());
            if (!queryStartTime.isEmpty()) {

                Long startTime = queryStartTime.firstKey();
                String startQueryNo = queryStartTime.get(startTime);
                Intent startService = new Intent("dataRequest");
                startService.putExtra("intent", Constants.START_DATA_REQUEST);
                startService.putExtra(Constants.QUERY_NO, startQueryNo);
                startService.putExtra(Constants.FILENAME,Environment.getExternalStorageDirectory().getAbsolutePath()+"/Mew/DataCollection/"+query.getQueryNo()+"/"+query.getSensorName()+".csv");
                startService.setAction(Constants.SENSING_ACTION);
                PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, Constants.SENSOR_SERVICE_START_ID, startService, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager startAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                startAlarm.set(AlarmManager.RTC_WAKEUP, startTime, startPendingIntent);
                Log.d(TAG, "Pending Start Intent- " + startTime + " Query# " + startQueryNo + " len(StartQueue) " + queryStartTime.size());
            }
            //this intent is being passed to onStartCommand()
            Intent intent = new Intent(context, SensorReadings.class);
            intent.putExtra(Constants.REQUESTED_SENSOR_NAME, sensorName);
            intent.putExtra(Constants.QUERY_NO, queryNo);
            intent.putExtra(Constants.ROUTING_KEY,query.getRoutingKey());
            intent.putExtra(Constants.FILENAME,Environment.getExternalStorageDirectory().getAbsolutePath()+"/Mew/DataCollection/"+query.getQueryNo()+"/"+query.getSensorName()+".csv");
            context.startService(intent);
        } else if (requestType.equalsIgnoreCase(Constants.STOP_DATA_REQUEST)) {
            // type stop
            // remove the files from queue
            // publish the data
            // update the stop intent
            // if no more stop intents then exit
            boolean collectionSucess = false;
            Log.d(TAG, "Stop Type Request QueryID " + query.getId());
            queryStopTime.remove(query.getEndTime());
            Pair<BufferedWriter, File> ioPair = fileMap.remove(query.getQueryNo());
            try {
                if (ioPair.getLeft() != null) {
                    ioPair.getLeft().close();
                    collectionSucess = true;
//                    Log.d(TAG, "Data Collected For Query# " + query.getQueryNo());
                } else {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteQuery(query.getId());
                    db.closeDb();
                Map<String, Object> queryServiced = new HashMap<>();
				queryServiced.put("TYPE", Constants.MESSAGE_TYPE.NOPROCESSING.getValue());
				queryServiced.put("NODE", Constants.DEVICE_ID);
				queryServiced.put("QUERY", query.getQueryNo());
				RabbitMQConnections publishResource = RabbitMQConnections.getInstance(context);
				publishResource.addMessageToQueue(queryServiced, Constants.RESOURCE_ROUTING_KEY);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //code to send collected data to the requester
            if (collectionSucess) {
                try {
//                    PublishSensorData.sendAsMessage(appContext, ioPair.getRight(), query.getQueryNo(), query.getSensorName(), query.getRoutingKey());
                    Map<String, Object> queryServiced = new HashMap<>();
                    queryServiced.put("TYPE", Constants.MESSAGE_TYPE.QUERYSERVICED.getValue());
                    queryServiced.put("NODE", Constants.DEVICE_ID);
                    queryServiced.put("QUERY", query.getQueryNo());
                    RabbitMQConnections publishResource = RabbitMQConnections.getInstance(context);
                    publishResource.addMessageToQueue(queryServiced, Constants.RESOURCE_ROUTING_KEY);

//                    Log.d(TAG, "Deleted From DB QueryID " + query.getId());
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteQuery(query.getId());
                    db.closeDb();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopCollection(sensorName);
            }

            if (queryStopTime.isEmpty()) {
                Intent intent = new Intent(context, SensorReadings.class);
                context.stopService(intent);
                return;
            } else {
                Long endTime = queryStopTime.firstKey();
                String stopQueryNo = queryStopTime.get(endTime);
//				Log.d(TAG, "updating Stop Pending Intent " + endTime + " for queryNo " + stopQueryNo);
                Log.d(TAG, "Pending Stop Intent- " + endTime + " Query# " + stopQueryNo + " len(queryStopTime) " + queryStopTime.size());
                Intent stopService = new Intent("dataRequest");
                stopService.putExtra("intent", Constants.STOP_DATA_REQUEST);
                stopService.putExtra(Constants.QUERY_NO, stopQueryNo);
                stopService.putExtra(Constants.FILENAME,fileSent);
                stopService.setAction(Constants.SENSING_ACTION);
                PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, Constants.SENSOR_SERVICE_STOP_ID, stopService, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager stopAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                stopAlarm.set(AlarmManager.RTC_WAKEUP, endTime, stopPendingIntent);
            }

        }
    }


    public static void stopCollection(String sensorName) {
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.ACCELEROMETER.getValue())) {
            accelerometerRecord.stop();
        }
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.WIFI.getValue())) {
            wifiStateRecord.stop();
        }
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.MIC.getValue())) {
            micRecord.stop();
        }
//        Log.e("TYPE","Need to call stop() here");
    }

    private static Pair<BufferedWriter, File> createFile(String queryNo, String sensorName) {

        File directory;
        File file;
        Pair<BufferedWriter, File> ioPair;
        directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/DataCollection/").getPath(), queryNo);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        file = new File(directory, sensorName + ".csv");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e1) {
                Log.e(TAG, "File creation failed for QueryNo " + queryNo);
                e1.printStackTrace();
            }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            ioPair = new Pair<>(writer, file);
            Log.d(TAG, "File created for queryNo " + queryNo);
            return ioPair;
        } catch (IOException e) {
            Log.e(TAG, "File creation failed for QueryNo " + queryNo);
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        myID = Constants.SERVICE_START_ID;
//        Intent intent = new Intent(this, SensorReadings.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Log.v(TAG, "Sensor Data Collection Service Created ");

    }

    @Override
    public int onStartCommand(Intent intent_received, int flags, int startId) {
        //create file with the requested queryno
        queryIntent = intent_received;
        sensorName = queryIntent.getExtras().getString(Constants.REQUESTED_SENSOR_NAME);
        queryNo = queryIntent.getExtras().getString(Constants.QUERY_NO);
//        Log.e("intent URI", intent_received.toUri(0));
        String file=queryIntent.getExtras().getString(Constants.FILENAME);
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.ACCELEROMETER.getValue())) {
            Log.e("DATA","Recording started");
            accelerometerRecord = new AccelerometerRecord();
            accelerometerRecord.start(this, 20000, queryNo,file);
        }
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.WIFI.getValue())) {
            Log.d(TAG, "WIFI");
            wifiStateRecord = new WifiStateRecord();
            wifiStateRecord.startRecording(this, 1000, queryNo,file);
        }
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.MIC.getValue())) {
            Log.d(TAG, "MIC");
            micRecord = new MicRecord();
            micRecord.startRecording(this, 20000, queryNo,file);
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d(TAG, "Calling onDestroy !!!");//this is called only when the last stopping intent is called. We need to publish more data if left
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.ACCELEROMETER.getValue())) {
            accelerometerRecord.stop();
        }
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.WIFI.getValue())) {
            wifiStateRecord.stop();
        }
        if (sensorName.equalsIgnoreCase(Constants.SENSORS.MIC.getValue())) {
           micRecord.stop();
        }

        Map<String, Object> setServicing = new HashMap<>();
        setServicing.put("TYPE", Constants.MESSAGE_TYPE.ENDSERVICING.getValue());
        setServicing.put("NODE", Constants.DEVICE_ID);
        RabbitMQConnections rabbitMQ = RabbitMQConnections.getInstance(this);
        rabbitMQ.addMessageToQueue(setServicing, Constants.RESOURCE_ROUTING_KEY);

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
