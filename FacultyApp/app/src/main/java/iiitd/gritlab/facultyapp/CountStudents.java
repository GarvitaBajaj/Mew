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
import java.util.Arrays;

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
        String queryNo = strings[strings.length-1];
        String[] macAddresses=strings;
        File directory;
        directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/ReceivedFile/").getPath(), queryNo);
        if (!directory.exists()) {
            received = false;
        } else {
//            System.out.println("directory.listFiles = " + directory.list().length);
            for (File f : directory.listFiles()) {
//                System.out.println("f.getName() = " + f.getName());
                CSVReader reader;
                try {
                    reader = new CSVReader(new FileReader(f));
                    String[] nextLine;
                    boolean found=false;
                    while ((nextLine = reader.readNext()) != null) {
                        String macID = nextLine[3].trim();
                        System.out.println("macID = " + macID);
                        for (String address : macAddresses) {
                            if (address.substring(0, 16).equals(macID.substring(0, 16))) {
//                                System.out.println("address.substring(0,15) = " + address.substring(0,16));
//                                System.out.println("macID = " + macID.substring(0,16));
//                                System.out.println("Counter incremented with file: " + f.getName());
                                count = count + 1;
                                found=true;
                                break;
                            }
                        }
                        if(found)
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                f.delete();
            }
//            System.out.println("count = " + count);
//            System.out.println("Deleting Directory. Success = "+directory.delete());
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
