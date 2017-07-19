package recruitment.iiitd.edu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;
import recruitment.iiitd.edu.mew.ExtractParameters;

/**
 * Created by garvitab on 29-10-2015.
 */
public class MobilityTrace extends Thread {

	private static Context mContext;
	Context context;
	private static final String TAG = "MobilityTraces";
	public static SharedPreferences sharedPref = null;
	public static final String LAST_TRACE = "mew.last.mobility.trace";

	private static MobilityTrace mobilityTrace;

	protected MobilityTrace(Context context) {
		this.context = context;
		mContext = context;
	}
	public static MobilityTrace getInstance(Context context) {
		mContext = context;
		if (mobilityTrace == null) {
			mobilityTrace = new MobilityTrace(context);
			mobilityTrace.start();
			Log.d(TAG, "started mobility traces!!");
		}
		if(sharedPref == null){
			sharedPref = context.getSharedPreferences("MobilityTraces", Context.MODE_PRIVATE);
		}
		return mobilityTrace;
	}
	public void run() {
		try {
			CSVReader reader;
			switch(Constants.DEVICE_ID) {

				case("d0dbd90ba2c6b816"):
				case("874ef3787f393cd2"):
				case("d3b99c5c1be691d0"):
					reader = new CSVReader(new InputStreamReader(context.getAssets().open("mobility_trace_6.csv")));
					Log.d("mobility","trace6");
					break;
				case("c72706c39647bc10"):
				case("a348016717671f24"):
				case("5b09a9fc25b1a66e"):
					reader = new CSVReader(new InputStreamReader(context.getAssets().open("mobility_trace_2.csv")));
					Log.d("mobility","trace2");
					break;
				case("d5d28042b406772d"):
				case("8d7ed6d3ee453134"):
				case("98728455d9ec50cd"):
					reader = new CSVReader(new InputStreamReader(context.getAssets().open("mobility_trace_3.csv")));
					Log.d("mobility","trace3");
					break;
				case("4a5395f3320fa158"):
				case("a30c120312e2ecc9"):
				case("9d6706888645b1aa"):
					reader = new CSVReader(new InputStreamReader(context.getAssets().open("mobility_trace_4.csv")));
					Log.d("mobility","trace4");
					break;
				case("59c30ba85acb465f"):
				case("44792dfb2b2301b4"):
				case("1b93c9dc3a9f8f9c"):
					reader = new CSVReader(new InputStreamReader(context.getAssets().open("mobility_trace_5.csv")));
					Log.d("mobility","trace5");
					break;
				default:
					reader = new CSVReader(new InputStreamReader(context.getAssets().open("mobility_trace_1.csv")));
					Log.d("mobility","trace1");
					System.err.println("Implementing the default mobility trace here");
			}
			String[] row = reader.readNext();


			Long lastTime = null;
			if(sharedPref == null){
				sharedPref = mContext.getSharedPreferences("MobilityTraces", Context.MODE_PRIVATE);
			}
			if(sharedPref != null){
				try {
					lastTime = Long.valueOf(sharedPref.getInt(LAST_TRACE, 0));
					Log.d(TAG , "Existing value in SP= " + lastTime );
				}catch(ClassCastException e){
					LogTimer.error("Preference already with this name");
//					FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
//					FirebaseCrash.report(e);
				}
				while(row != null && lastTime != 0){
					long temp_time = Long.valueOf(row[3]);
					if( lastTime == temp_time) {
						Log.d(TAG, "Found time in CSV" + lastTime );
						break;
					}
					row = reader.readNext();
				}
			}

			long basetime = Long.valueOf(row[3]);
			long i = 0;
			while (row != null) {
				int timeVar=0;
				long rowTime=Math.abs(Long.valueOf(row[ 3 ]) - basetime);

				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putInt(LAST_TRACE, Integer.parseInt(row[3]));
				editor.commit();
				while (i<rowTime)
				{
					Thread.sleep(1000);
					i=i+1;
				}
				if (i == Math.abs(Long.valueOf(row[ 3 ]) - basetime)) {
					Location loc = new Location("Test");
					loc.setLatitude(Double.valueOf(row[ 0 ]));
					loc.setLongitude(Double.valueOf(row[ 1 ]));
					loc.setTime(System.currentTimeMillis());
					loc.setAltitude(0);
					loc.setTime(System.currentTimeMillis());
					loc.setAccuracy(0.0f);
					loc.setElapsedRealtimeNanos(System.currentTimeMillis());
					ExtractParameters.locManager.setTestProviderLocation("Test", loc);
				}

				row=reader.readNext();
			}
		} catch (Exception e) {
			LogTimer.blockingDeque.add(System.currentTimeMillis()+" "+ this.getClass().toString()+ " "+e.getMessage());
			e.printStackTrace();
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
//			FirebaseCrash.report(e);
		}
	}
}
