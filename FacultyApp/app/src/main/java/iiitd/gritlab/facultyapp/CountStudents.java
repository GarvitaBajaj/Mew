package iiitd.gritlab.facultyapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by garvita on 25-11-2017.
 */

public class CountStudents extends AsyncTask<String, Void, String> {

    boolean received = true;
    int count = 0;
    Context mContext;

    public CountStudents(Context context){
        mContext = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String queryNo = strings[0];
        String macAddress=strings[1];
        File directory;
        directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/ReceivedFile/").getPath(), queryNo);
        if (!directory.exists()) {
            received = false;
        } else {
            System.out.println("directory.listFiles = " + directory.list().length);
            for (File f : directory.listFiles()) {
                CSVReader reader;
                try {
                    reader = new CSVReader(new FileReader(f));
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        String macID=nextLine[3].trim();
                        System.out.println("macID = " + macID);
                        System.out.println("macAddress = " + macAddress);
                            if(macID.equals(macAddress)) {
                                System.out.println("Counter incremented");
                                count=count+1;
                                break;
                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                f.delete();
            }
            System.out.println("count = " + count);
            System.out.println("Deleting Directory. Success = "+directory.delete());
        }
        return String.valueOf(count);
    }

    @Override
    protected void onPostExecute(String a)
    {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Toast.makeText(mContext, "Students around: "+a, Toast.LENGTH_LONG).show();
        GraphCount.populateSeries(Integer.parseInt(a));
    }

}
