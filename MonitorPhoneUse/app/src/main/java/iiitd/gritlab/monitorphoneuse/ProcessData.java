package iiitd.gritlab.monitorphoneuse;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by garvita on 11-12-2017.
 */

public class ProcessData extends AsyncTask<String, Void, String> {

    Context mContext;

    public ProcessData(Context context){
        mContext = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String queryNo = strings[0];
        File directory;
        boolean received;
        int count=0;
        directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/ReceivedFile/").getPath(), queryNo);
        if (!directory.exists()) {
            received = false;
        } else {
            System.out.println("directory.listFiles = " + directory.list().length);
            for (File f : directory.listFiles()) {
                double mean=0.0;
                int distractions=0;
                //calculate the distractions
                String line = "";
                String splitBy = ",";
                try{
                    HashMap<Long, Double> timeseries=new HashMap();
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    while ((line = br.readLine()) != null) {
                        String[] reading = line.split(splitBy);
                        double x = Double.parseDouble(reading[1]);
                        double y = Double.parseDouble(reading[2]);
                        double z = Double.parseDouble(reading[3]);

                        if (x > 5) {
                            x = x - 9.8;
                        }
                        if (y > 5) {
                            y = y - 9.8;
                        }
                        if (z > 5) {
                            z = z - 9.8;
                        }

                        double acc = Math.sqrt(x * x + y * y + z * z);
                        timeseries.put(Long.parseLong(reading[0]), acc);
                    }
                    ArrayList<Long> keySet = new ArrayList<>(timeseries.keySet());
                    ArrayList<Double> listOfValues=new ArrayList<>(timeseries.values());
                    //take a window of 10 seconds
                    int window=10*1000/20; //in miliseconds - frequency of reading: 20 ms
                    double sd=0.0,oldsd;
                    for(int i=0;i<listOfValues.size()-window;i++){
                        mean=getMean(i,window,listOfValues);
                        oldsd=sd;
                        sd=getSD(i,window,listOfValues, mean);
                        if(oldsd-sd>0.02){
                            System.out.println("distraction here:");
                            distractions++;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                f.delete();
                if(distractions>0)
                    count++;
            }
            System.out.println("Deleting Directory. Success = "+directory.delete());
        }
        return String.valueOf(count);
    }

    double getMean(int i, int window, ArrayList<Double> listOfValues){
        double sum=0.0;
        for(int q=i;q<=i+window;q++){
            sum=sum+listOfValues.get(q);
        }
        return sum/window;
    }

    double getSD(int i, int window, ArrayList<Double> listOfValues, double mean){
        double sd=0.0;
        double diff=0.0, tempsum=0.0;
        //for each element, calculate the difference.
        for(int q=i;q<=i+window;q++){
            diff=(mean-listOfValues.get(q));
            tempsum=tempsum+diff*diff;
        }
        sd=Math.sqrt(tempsum/window);
//        System.out.println("sd = " + sd);
//        System.out.println("mean = " + mean);
        return sd;
    }


    @Override
    protected void onPostExecute(String a)
    {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Toast.makeText(mContext, "Students distracted: "+a, Toast.LENGTH_LONG).show();
        DistractionCount.populateSeries(Integer.parseInt(a));
    }
}
