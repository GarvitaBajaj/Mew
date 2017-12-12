package iiitd.gritlab.monitorphoneuse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import recruitment.iiitd.edu.mew.HomeScreen;
import recruitment.iiitd.edu.mew.SettingsActivity;
import recruitment.iiitd.edu.model.Query;


public class MainActivity extends AppCompatActivity {

    EditText ed1;//ed2,ed3;
    int aRequestCode=2234;
    Context mContext;
    static AlarmManager alarmMgr;
    static PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;
        ed1=(EditText)findViewById(R.id.editText);
        //start Homescreen of library for initialising the variables and starting the subscriber thread
        Intent i=new Intent(this, HomeScreen.class);
        i.putExtra("returnResult",true);
        startActivityForResult(i,aRequestCode);
    }

    @Override
    protected void onActivityResult( int aRequestCode, int aResultCode, Intent aData ) {
        super.onActivityResult(aRequestCode, aResultCode, aData);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fireQueries(View view) {
        System.out.println("Firing query");
        int duration = Integer.parseInt(ed1.getText().toString()) * 60000;
        Query q = new Query(mContext);
        q.setSensorName("Accelerometer");
        q.setFromTime(System.currentTimeMillis() + 60000); //one minute from now, start issuing requests
        q.setToTime(q.getFromTime() + duration);
        q.setMin(0);
        q.setSelection("broadcast");
        JSONObject jsonQuery = Query.generateJSONQuery(q);
        try {
            Query.sendQueryToServer(jsonQuery, mContext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent next=new Intent(this, DistractionCount.class);
        next.putExtra("queryNo",q.getQueryNo());
        next.putExtra("duration",duration);
        startActivity(next);
    }
}
