package iiitd.gritlab.facultyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import recruitment.iiitd.edu.model.Query;

/**
 * Created by garvita on 27-11-2017.
 */

public class FireQuery extends BroadcastReceiver{

    static MonitorQueries directoryFileObserver;
    String MewPath=Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mew/ReceivedFile/";
    @Override
    public void onReceive(final Context context, Intent intent) {
//        System.out.println("Firing query");
        int duration=intent.getIntExtra("duration",60000);
        Query q = new Query(context);
        q.setSensorName("WiFi");
        q.setFromTime(System.currentTimeMillis() + 60000); //one minute from now, start issuing requests
        q.setToTime(q.getFromTime() + duration);
        q.setMin(0);
        q.setSelection("broadcast");
        JSONObject jsonQuery = Query.generateJSONQuery(q);
        try {
            Query.sendQueryToServer(jsonQuery, context);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String lectureHall=intent.getStringExtra("lectureHall");
        List<String> macIDs=APLabels.labels.get(lectureHall);
        final String[] params = macIDs.toArray(new String[macIDs.size()+1]);
        params[macIDs.size()]=q.getQueryNo();// {q.getQueryNo(),macIDs.toArray()};
//        System.out.println("params = " + Arrays.toString(params));
        String pathname= MewPath.concat("9d6706888645b1aa1515079026783");
        System.out.println("pathname = " + pathname);
        directoryFileObserver = new MonitorQueries(pathname, FileObserver.CREATE); //this should be called once the file directory has been created. Calling it before does nothing when the event happens
        directoryFileObserver.startWatching();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CountStudents countStudents = new CountStudents(context);
                countStudents.execute(params);
            }
        }, duration+120000);
    }


}
