//package iiitd.gritlab.facultyapp;
//
//import android.app.IntentService;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Environment;
//import android.support.annotation.Nullable;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileReader;
//
//import au.com.bytecode.opencsv.CSVReader;
//
///**
// * Created by garvita on 27-11-2017.
// */
//
//public class CountStudentsBgd extends IntentService {
//
//    final static String KEY_INT_FROM_SERVICE = "KEY_INT_FROM_SERVICE";
//    final static String KEY_STRING_FROM_SERVICE = "KEY_STRING_FROM_SERVICE";
//    final static String ACTION_UPDATE_CNT = "UPDATE_CNT";
//    final static String ACTION_UPDATE_MSG = "UPDATE_MSG";
//
//    final static String KEY_QUERYNO = "queryNo";
//    final static String KEY_MACID = "macID";
//    MyServiceReceiver resultReceiver;
//    final static String ACTION_MSG_TO_SERVICE = "MSG_TO_SERVICE";
//
//     public CountStudentsBgd(){
//         super("test");
//     }
//    @Override
//    public void onCreate() {
//        Toast.makeText(getApplicationContext(),
//                "onCreate", Toast.LENGTH_LONG).show();
//        resultReceiver = new MyServiceReceiver();
//        super.onCreate();
//    }
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(getApplicationContext(),
//                "onStartCommand", Toast.LENGTH_LONG).show();
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ACTION_MSG_TO_SERVICE);
//        registerReceiver(resultReceiver, intentFilter);
//
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onDestroy() {
//        Toast.makeText(getApplicationContext(),
//                "onDestroy", Toast.LENGTH_LONG).show();
//        unregisterReceiver(resultReceiver);
//        super.onDestroy();
//    }
//
//    boolean received = true;
//    int count = 0;
//    Context mContext;
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        System.err.println("Intent handling");
//        String queryNo = intent.getStringExtra("queryNo");//strings[0];
//        String macAddress=intent.getStringExtra("macID");//strings[1];
//        File directory;
//        directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/ReceivedFile/").getPath(), queryNo);
//        if (!directory.exists()) {
//            received = false;
//        } else {
//            System.out.println("directory.listFiles = " + directory.list().length);
//            for (File f : directory.listFiles()) {
//                CSVReader reader;
//                try {
//                    reader = new CSVReader(new FileReader(f));
//                    String[] nextLine;
//                    while ((nextLine = reader.readNext()) != null) {
//                        String macID=nextLine[3].trim();
//                        System.out.println("macID = " + macID);
//                        System.out.println("macAddress = " + macAddress);
//                        if(macID.equals(macAddress)) {
//                            System.out.println("Counter incremented");
//                            count=count+1;
//                            break;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                f.delete();
//            }
//            System.out.println("count = " + count);
//            System.out.println("Deleting Directory. Success = "+directory.delete());
//        }
//        Intent iPrompt = new Intent();
//        iPrompt.setAction(ACTION_UPDATE_MSG);
//        iPrompt.putExtra(KEY_STRING_FROM_SERVICE, "testResultReturned");
//        sendBroadcast(iPrompt);
//        System.out.println("Sending data to listener");
//    }
//
//    public class MyServiceReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//            if(action.equals(ACTION_MSG_TO_SERVICE)){
//                String msg = intent.getStringExtra(KEY_QUERYNO);
//
//                msg = new StringBuilder(msg).reverse().toString();
//
//                //send back to MainActivity
//                Intent i = new Intent();
//                i.setAction(ACTION_UPDATE_MSG);
//                i.putExtra(KEY_STRING_FROM_SERVICE, msg);
//                sendBroadcast(i);
//            }
//        }
//    }
//}
