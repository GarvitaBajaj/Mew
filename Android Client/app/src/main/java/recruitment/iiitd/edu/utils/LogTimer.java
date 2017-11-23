//package recruitment.iiitd.edu.utils;
//
//import android.app.ActivityManager;
//import android.content.Context;
//import android.net.TrafficStats;
//import android.os.Debug;
//import android.os.Environment;
//import android.os.Handler;
//import android.util.Log;
//
////import com.google.firebase.crash.FirebaseCrash;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.LinkedBlockingDeque;
//
//import recruitment.iiitd.edu.rabbitmq.SubscribeReceiver;
//
///**
// * Created by garvitab on 21-10-2015.
// */
//public class LogTimer  {
//
//	public static BlockingDeque<String> blockingDeque;
//
//
//
//	private static FileOutputStream stream;
//
//	Context context;
//	final Handler handler = new Handler();
//	static Timer timer;
//	static TimerTask timerTask;
//	private static File loggingFile, directory;
//
//	public static boolean error(String error){
//
//		Long time = System.currentTimeMillis();
//
//		String error_row = time + " , " + " ERROR ,"+  error +"\n";
//
//		return blockingDeque.add(error_row);
//	}
//
//	public LogTimer(Context context){
//		this.context=context;
//		blockingDeque=new LinkedBlockingDeque<>();
//	}
//
//	void deleteRecursive(File fileOrDirectory) {
//		if(fileOrDirectory.exists()) {
//			if (fileOrDirectory.isDirectory())
//				for (File child : fileOrDirectory.listFiles())
//					deleteRecursive(child);
//
//			fileOrDirectory.delete();
//		}
//	}
//
//	public void startTimer() {
//
//		timer = new Timer();
//		initializeTimerTask();
//		long startTime = System.currentTimeMillis() % 1000;
//		directory=new File(Environment.getExternalStorageDirectory()+"/Mew/");
//		deleteRecursive(directory);
//		directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/").getPath(), "Logging");
//
//		if (!directory.exists()) {
//			directory.mkdirs();
//		}
//
//		loggingFile = new File(directory, "logging.csv");
//
//		if (!loggingFile.exists()) {
//			try {
//				loggingFile.createNewFile();
//				Log.d("TT", "creating logging.csv");
//			} catch (IOException e) {
//				e.printStackTrace();
//				LogTimer.error(this.getClass().toString() + " : " + e.getMessage());
////				FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////				FirebaseCrash.report(e);
//			}
//		}
//
//		try {
//			stream = new FileOutputStream(loggingFile, true);
//
////			Log.d("TT", "FileOutputStream initialized");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			LogTimer.error(this.getClass().toString() + " : " + e.getMessage());
////			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////			FirebaseCrash.report(e);
//		}
//		//schedule the timer for 10 seconds starting at the next second (subtract the extra time from
//		//the current time and then start at the next second's onset
//		timer.schedule(timerTask, startTime, 10000);
//	}
//
//	public void stoptimertask() {
//		//stop the timer, if it's not already null
//		if (timer != null) {
//			timer.cancel();
//			timer = null;
//		}
//	}
//
//	public void initializeTimerTask() {
//
////		Log.d("TT", "inside initializeTimerTask");
//
//		timerTask = new TimerTask() {
//			public void run() {
//
//				//use a handler to run a toast that shows the current timestamp
//				handler.post(new Runnable() {
//					public void run() {
//						try {
//							String memory_row = "";
//							String data_transmission = "";
//							long tx = TrafficStats.getTotalTxBytes();
//							long rx = TrafficStats.getTotalRxBytes();
//
//							Long time = System.currentTimeMillis();
//
//							ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//							int[] ids = {android.os.Process.myPid()};
//							Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(ids);
//
//							while (blockingDeque.size() > 0) {
//								String log_row = blockingDeque.poll();
//								stream.write(log_row.getBytes());
//								stream.flush();
//							}
//							memory_row += time + " , " + " MEM ,"+ SubscribeReceiver.runningServices.get() + " , " + memInfo[ 0 ].getTotalPss() + "KB , " + memInfo[ 0 ].dalvikPrivateDirty + " , " + memInfo[ 0 ].nativePss + "\n";
//							data_transmission += time + " , " + " DATA ,"+ tx + " , " + rx+"\n";
//							stream.write(memory_row.getBytes());
//							stream.write(data_transmission.getBytes());
//							stream.flush();
//						} catch (IOException e1) {
//							e1.printStackTrace();
////							FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
////							FirebaseCrash.report(e1);
//						}
//					}});
//			}
//		};
//	}
//}
