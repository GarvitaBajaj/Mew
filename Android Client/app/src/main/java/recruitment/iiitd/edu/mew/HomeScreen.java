package recruitment.iiitd.edu.mew;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
//import android.support.v7.app.ActionBarActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

//import com.google.firebase.crash.FirebaseCrash;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import recruitment.iiitd.edu.contextAwareness.ActivityRecognitionService;
import recruitment.iiitd.edu.contextAwareness.UserActivity;
import recruitment.iiitd.edu.model.DatabaseHelper;
import recruitment.iiitd.edu.model.Query;
import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;
import recruitment.iiitd.edu.utils.MobilityTrace;

import com.google.android.gms.awareness.Awareness;
//import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.tasks.Task;


public class HomeScreen extends Activity {

    private final String TAG = this.getClass().getCanonicalName();
    private Switch resourceProvider, queryListener;
    boolean mockLocation = false;
    File directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/").getPath(), "Query_Timestamps");
    LogTimer logTimer;
    Intent i1;
    Context mContext;
    public boolean screenAlarms = false;
    //varibles specific to locking/unlocking the screen at regular intervals
//    public static WifiManager wm;
//    public static WifiManager.WifiLock wifiLock;
    public static PowerManager mPowerManager;
    public static PowerManager.WakeLock mWakeLock;
    public static DevicePolicyManager deviceManger;
    public static ActivityManager activityManager;
//    public static ComponentName compName;
//    static final int RESULT_ENABLE = 1;

    private void setNeverSleepPolicy() {
        try {
            ContentResolver cr = getContentResolver();
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                int set = android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER;
                android.provider.Settings.System.putInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, set);
            } else {
                int set = android.provider.Settings.Global.WIFI_SLEEP_POLICY_NEVER;
                android.provider.Settings.System.putInt(cr, android.provider.Settings.Global.WIFI_SLEEP_POLICY, set);
            }

        } catch (Exception e) {
            LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    //enable device administrator for this app (check the box using UI in Security-> Device Administrators)
//    public void enableAdmin() {
//        Intent intent = new Intent(DevicePolicyManager
//                .ACTION_ADD_DEVICE_ADMIN);
//        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
//                compName);
//        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Additional text explaining why this needs to be added.");
//        startActivityForResult(intent, RESULT_ENABLE);
//    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case RESULT_ENABLE:
//                if (resultCode == Activity.RESULT_OK) {
//                    Log.i("DeviceAdminSample", "Admin enabled!");
////				} else {
////					Log.e("DeviceAdminSample", "UNABLE TO ADD ADMIN");
//                }
//                return;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    public void wakeUpScreenPeriodically() {
        mPowerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
        AlarmManager startAlarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StartScreen.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Constants.FIRE_SCREEN_ON, intent, 0);
        startAlarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Constants.SCREEN_INTERVAL, pendingIntent);
        AlarmManager stopAlarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent stopIntent = new Intent(this, StopScreen.class);
        PendingIntent pendingIntentstop = PendingIntent.getBroadcast(this, Constants.FIRE_SCREEN_OFF, stopIntent, 0);
        stopAlarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + Constants.SCREEN_DURATION, Constants.SCREEN_INTERVAL, pendingIntentstop);
        screenAlarms = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        compName = new ComponentName(this, MyAdmin.class);
//		enableAdmin();
//		wakeUpScreenPeriodically();
        mContext = this;
//		setNeverSleepPolicy();
        mockLocation = false;


//		if(Build.VERSION.SDK_INT>=17) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int id) {
//					mockLocation = true;
//					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//							MobilityTrace.sharedPref = mContext.getSharedPreferences("MobilityTraces", Context.MODE_PRIVATE);
//							SharedPreferences.Editor editor = MobilityTrace.sharedPref.edit();
//							editor.putInt(MobilityTrace.LAST_TRACE, 0);
//							editor.commit();
//
//						}
//					});
//					builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//							return;
//						}
//					});
//
//					builder.setMessage("Do you want to start from beginning?");
//					builder.setTitle("Location Alert");
//					AlertDialog lDialog = builder.create();
//					lDialog.show();
//					SharedPreferences.Editor editor=getApplicationContext().getSharedPreferences("StateValues", Context.MODE_PRIVATE).edit();
//					editor.putBoolean("mockLocation",true);
//					editor.commit();
//				}
//			});
//			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int id) {
//					mockLocation = false;
//				}
//			});
//
//			builder.setMessage("Do you want to enable mock locations for this session?");
//			builder.setTitle("Location Alert");
//			AlertDialog dialog = builder.create();
//			dialog.show();
//		}

        Constants.DEVICE_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

//        TextView deviceID = (TextView) findViewById(R.id.textView4);
//        deviceID.setText(Constants.DEVICE_ID);

        final RabbitMQConnections rabbitMQ = RabbitMQConnections.getInstance(this);
        rabbitMQ.publishToAMQP();

        rabbitMQ.subscribe();

        i1 = new Intent(this, ExtractParameters.class);

//		resourceProvider = (Switch) findViewById(R.id.mySwitch);
//		queryListener = (Switch) findViewById(R.id.mySwitch2);

        if (savedInstanceState != null) {
//			resourceProvider.setChecked(savedInstanceState.getBoolean("service1"));
//			queryListener.setChecked(savedInstanceState.getBoolean("service2"));
        }

//		resourceProvider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

//			@Override
//			public void onCheckedChanged(CompoundButton buttonView,
//										 boolean isChecked) {
//				if (isChecked) {
//					mActivityRecognitionClient.requestActivityUpdates(5*60*1000,PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), UserActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
//        Log.d(TAG, "Connecting to Google API");
//        if (wifiLock != null) { // May be null if wm is null
//            if (!wifiLock.isHeld()) {
//                wifiLock.acquire();
////                System.out.println(wifiLock.isHeld() + "");
////                Log.e("WIFI LOCK", "Acquired");
//            }
//        }
//        i1.putExtra("mockLocation", mockLocation);
        startService(i1);

//				} else {
//					Log.e("WIFI",wifiLock.toString());
//					if (wifiLock.isHeld()) {
//						wifiLock.release();
//						Log.e("WIFI LOCK", "RELEASED");
//					}
//					stopService(i1);
//					Log.d(Constants.TAG, "SERVICE STOPPED");
//				}
//			}
//		});

//		queryListener.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView,
//										 boolean isChecked) {
//
//				if (isChecked) {
//        Map<String, Object> states = new HashMap<String, Object>();
//        states.put("TYPE", Constants.MESSAGE_TYPE.PROVIDER.getValue());
//        states.put("NODE", Constants.DEVICE_ID);
//        rabbitMQ.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);
//				} else {
//
//				}
//
//			}
//		});

        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("StateValues", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("DEVICEID", Constants.DEVICE_ID);
        edit.commit();
//        Map<String, Object> states = new HashMap<String, Object>();
//        states.put("TYPE", Constants.MESSAGE_TYPE.PROVIDER.getValue());
//        states.put("NODE", Constants.DEVICE_ID);
//        rabbitMQ.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);
        logTimer = new LogTimer(this);
        LogTimer.blockingDeque.add(System.currentTimeMillis() + ":" + "Application_started" + "\n");
        logTimer.startTimer();
        finish();
    }

    public void stopContributing(View v) {
        stopService(i1);
//        Log.d(Constants.TAG, "SERVICE STOPPED");
        DatabaseHelper db = new DatabaseHelper(this);
        db.removeAll();
        Map<String, Object> states = new HashMap<String, Object>();
        states.put("TYPE", Constants.MESSAGE_TYPE.LVNGLISTENER.getValue());
        states.put("NODE", Constants.DEVICE_ID);
        RabbitMQConnections rabbitMQ = RabbitMQConnections.getInstance(this);
        rabbitMQ.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);
//        if (wifiLock.isHeld())
//            wifiLock.release();
        if (screenAlarms) {
            if (mWakeLock.isHeld())
                mWakeLock.release();
            cancelScreenAlarms();
        }
        logTimer.stoptimertask();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void clearDB(View v) {
        DatabaseHelper db = new DatabaseHelper(this);
        db.removeAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
//		savedInstanceState.putBoolean("service1", resourceProvider.isChecked());
//		savedInstanceState.putBoolean("service2", queryListener.isChecked());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//		resourceProvider.setChecked(savedInstanceState.getBoolean("service1"));
//		queryListener.setChecked(savedInstanceState.getBoolean("service2"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            Intent i = new Intent(this, SettingsActivity.class);
//            startActivity(i);
//            return true;
//        }
//
//		if(id==R.id.action_query){
//			Intent i=new Intent(this,QueryForm.class);
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//			startActivity(i);
//		}
//
//		if(id==R.id.action_backup){
//
//			File file1 = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/").getPath());
//			final File to = new File(file1.getAbsolutePath() + System.currentTimeMillis());
//			file1.renameTo(to);
//
//			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//			intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"garvitab@iiitd.ac.in"});//,"apurv09064@iiitd.ac.in"});
//			intent.putExtra(Intent.EXTRA_SUBJECT, "Log file for "+Constants.DEVICE_ID);
//			intent.putExtra(Intent.EXTRA_TEXT, "This is an auto generated email. It contains the log file for the last experiment run.");
//			ArrayList<Uri> uris = new ArrayList<Uri>();
//
//			File directory = new File(to,"/Logging/");
//			File file=new File(directory,"logging.csv");
//			if (!file.exists() || !file.canRead()) {
//				Toast.makeText(this, "Logging.csv unavailable", Toast.LENGTH_SHORT).show();
//				finish();
//			}
//			Uri uri = Uri.parse("file://" + file);
//			uris.add(0,uri);
//			directory = new File(to,"/Query_Timestamps/");
//			file=new File(directory,"queries.csv");
//			if (!file.exists() || !file.canRead()) {
//				Toast.makeText(this, "Queries.csv unavailable", Toast.LENGTH_SHORT).show();
//			}
//			else {
//				uri = Uri.parse("file://" + file);
//				uris.add(1, uri);
//			}
//			file=new File(directory,"timestamps.csv");
//			if (!file.exists() || !file.canRead()) {
//				Toast.makeText(this, "timestamps.csv unavailable", Toast.LENGTH_LONG).show();
//			}
//			else {
//				uri = Uri.parse("file://" + file);
//				uris.add(2, uri);
//			}
//			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//			intent.setType("message/rfc822");
//			startActivity(intent);
//		}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void exit(View view) {
        //release all locks if held
//        if (wifiLock.isHeld())
//            wifiLock.release();
        if (screenAlarms) {
            if (mWakeLock.isHeld())
                mWakeLock.release();

            //cancel the alarms used to turn on/off the screens
            cancelScreenAlarms();
        }
        logTimer.stoptimertask();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void cancelScreenAlarms() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent updateOnIntent = new Intent(this, StartScreen.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Constants.FIRE_SCREEN_ON, updateOnIntent, 0);
        Intent updateOffIntent = new Intent(this, StopScreen.class);
        PendingIntent pendingIntentstop = PendingIntent.getBroadcast(this, Constants.FIRE_SCREEN_OFF, updateOffIntent, 0);
        // Cancel alarms
        try {
            alarmManager.cancel(pendingIntent);
            alarmManager.cancel(pendingIntentstop);
//            Log.d("Screen Alarms", "Cancelled");
        } catch (Exception e) {
            Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
            LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Cancelling screen alarms");
//			FirebaseCrash.report(e);
        }
    }

    public void automateQueries(View view) throws FileNotFoundException {

        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File file = new File(directory, "timestamps.csv");

        AlertDialog.Builder newQueries = new AlertDialog.Builder(this);
        newQueries.setTitle("New Queries");
        newQueries.setMessage("Do you want to generate new queries for this session?");
        newQueries.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //delete the old file and generate new queries here
                    if (file.exists())
                        file.delete();
                    file.createNewFile();
//                    Log.d(Constants.TAG, "Creating a new file to store timestamps");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                    Random random = new Random();

                    //get lat lon from mobility traces
                    //ensure that mobility trace is not the same as that of a participant
                    CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("mobility_trace_6.csv")));
                    List<String[]> allRow = reader.readAll();

                    int offset = 2 * 60 * 1000; //the delay in starting the query execution
                    int noQueries = 0;
                    while (noQueries < Constants.NUMBER_OF_QUERIES) {

                        noQueries++;
                        //todo move these to settings file
                        //todo remove offset for query generation
                        long startMin = random.nextInt(Constants.EXPERIMENT_DURATION);        //duration of experiment = 10 hours = 10*60 minutes

                        long startTime = (long) (startMin * 60 * 1000 + offset);    //converting the time to milliseconds
                        int duration = Constants.QUERY_DURATION;        //10 minutes of data collection
                        long endTime = startTime + duration;

                        Query query = new Query(getApplicationContext());
                        int row_index = random.nextInt(allRow.size());        //randomly read a row for generating the query location

                        try {
                            Double latitude = Double.parseDouble(allRow.get(row_index)[0]);
                            Double longitude = Double.parseDouble(allRow.get(row_index)[1]);
                            query.setLatitude(latitude);
                            query.setLongitude(longitude);
                            allRow.remove(allRow.get(row_index));        //remove that row so that the same location is not picked next time
                        } catch (Exception e) {
                            noQueries--;
//							Log.d("parsing exception", String.valueOf(row_index));
                            LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//							FirebaseCrash.logcat(Log.ERROR, "Exception Caught", "Error generating queries");
//							FirebaseCrash.report(e);
                            continue;
                        }

                        query.setMin(1);//random.nextInt(Constants.MAX_PROVIDERS-Constants.MIN_PROVIDERS+1) + Constants.MIN_PROVIDERS);
                        query.setMax(query.getMin());
                        query.setFromTime(startTime);
                        query.setToTime(endTime);
                        query.setSensorName("Accelerometer");
                        JSONObject jsonQuery = Query.generateJSONQuery(query);
                        writer.write(jsonQuery.toString());
                        writer.newLine();
                        Thread.sleep(1);
                    }
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e1.getMessage());
//					FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Sending queries");
//					FirebaseCrash.report(e1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//					FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Sending queries");
//					FirebaseCrash.report(e);
                }
                sendAutomaticQueries(file);
            }
        });
        newQueries.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendAutomaticQueries(file);
            }
        });
        newQueries.show();

    }

    public void sendAutomaticQueries(File file) {
        try {

            File file1 = new File(directory, "queries.csv");

            InputStream instream = new FileInputStream(file);
            if (file1.exists())
                file1.delete();

            file1.createNewFile();

            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                line = buffreader.readLine();

                FileOutputStream stream = new FileOutputStream(file1);

                while (line != null) {
                    JSONObject query = new JSONObject(line);
                    Query query1 = new Query(this);
                    query1.setFromTime(query.getLong("fromTime") + System.currentTimeMillis());
                    query1.setToTime(query.getLong("toTime") + System.currentTimeMillis());
                    query1.setLatitude(query.getDouble("latitude"));
                    query1.setLongitude(query.getDouble("longitude"));
                    query1.setMin(query.getInt("min"));
                    query1.setMax(query.getInt("max"));
                    query1.setSensorName(query.getString("dataReqd"));

                    JSONObject jsonQuery = Query.generateJSONQuery(query1);
                    stream.write(jsonQuery.toString().getBytes());
                    line = buffreader.readLine();
                    Query.sendQueryToServer(jsonQuery, this);
                    Thread.sleep(200); //issue a query every 2 seconds - this gives sufficient time for allocating queries using baseline algorithm
                }
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Sending queries");
//			FirebaseCrash.report(e);
        }

    }


}
