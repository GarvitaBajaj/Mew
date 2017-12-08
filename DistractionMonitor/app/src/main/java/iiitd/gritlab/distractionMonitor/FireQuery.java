package iiitd.gritlab.distractionMonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import recruitment.iiitd.edu.model.Query;

/**
 * Created by garvita on 27-11-2017.
 */

public class FireQuery extends BroadcastReceiver{
    @Override
    public void onReceive(final Context context, Intent intent) {
        System.out.println("Firing query");
        int duration=intent.getIntExtra("duration",60000);
        Query q = new Query(context);
        q.setSensorName("Accelerometer");
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
//        final String[] params = {q.getQueryNo(),intent.getStringExtra("macID")};
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CountStudents countStudents = new CountStudents(context);
//                countStudents.execute(params);
//            }
//        }, duration+120000);
    }


}
