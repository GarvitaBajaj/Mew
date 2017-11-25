package iiitd.gritlab.facultyapp;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import recruitment.iiitd.edu.mew.HomeScreen;
import recruitment.iiitd.edu.mew.SettingsActivity;
import recruitment.iiitd.edu.model.Query;

public class MainActivity extends AppCompatActivity {

    EditText ed1,ed2,ed3;
    int aRequestCode=1234;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_main);
        ed1=(EditText)findViewById(R.id.editText);
        ed2=(EditText)findViewById(R.id.editText2);
        ed3=(EditText)findViewById(R.id.editText6);
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
        if (matchIP()) {
            //save the query number for reference - data processing
            //check whether ReceivedFile folder is created properly
            //launch a timertask to issue regular queries
            Query q = new Query(this);
            q.setSensorName("WiFi");
            q.setFromTime(System.currentTimeMillis() + 60000); //one minute from now, start issuing requests
            int duration=Integer.parseInt(ed2.getText().toString())*1000*60;
            q.setToTime(q.getFromTime() + duration);
            q.setMin(0);
            q.setSelection("broadcast");
            JSONObject jsonQuery = Query.generateJSONQuery(q);
            try {
                Query.sendQueryToServer(jsonQuery, this);
                final String[] params = {q.getQueryNo(),ed3.getText().toString()};
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       new CountStudents(mContext).execute(params);
                    }
                }, duration+120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else{
            Toast.makeText(this,"Please enter a valid MAC address", Toast.LENGTH_SHORT).show();
        }
    }


    protected boolean matchIP(){
        String entered=ed3.getText().toString();
        String regexPattern="([0-9A-Fa-f]{2}[:-]?){5}([0-9A-Fa-f]{2})";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(entered);
        return matcher.matches();
   }
}
