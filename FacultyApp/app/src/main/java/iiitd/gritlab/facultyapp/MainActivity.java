package iiitd.gritlab.facultyapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import recruitment.iiitd.edu.mew.HomeScreen;
import recruitment.iiitd.edu.mew.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    EditText ed1,ed2;
    Spinner s1;
    int aRequestCode=1234;
    Context mContext;
    static AlarmManager alarmMgr;
    static PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_main);
        ed1=(EditText)findViewById(R.id.editText);
        ed2=(EditText)findViewById(R.id.editText2);
        addLectureHalls();
        APLabels.setLabels();
        //start Homescreen of library for initialising the variables and starting the subscriber thread
        Intent i=new Intent(this, HomeScreen.class);
        i.putExtra("returnResult",true);
        startActivityForResult(i,aRequestCode);
    }

    public void addLectureHalls() {
        s1 = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        list.add("C01");
        list.add("C02");
        list.add("C03");
        list.add("C11");
        list.add("C12");
        list.add("C13");
        list.add("C21");
        list.add("C22");
        list.add("C23");
        list.add("C24");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(dataAdapter);
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
//        if (matchIP()) {
             //fire query every time defined by the UI
            alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, FireQuery.class);
            intent.putExtra("duration",Integer.parseInt(ed2.getText().toString())*1000*60);
            intent.putExtra("lectureHall",String.valueOf(s1.getSelectedItem()));
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,0,Integer.parseInt(ed1.getText().toString())*60*1000, alarmIntent);
            Intent next=new Intent(this, GraphCount.class);
            startActivity(next);
//        }
//        else{
//            Toast.makeText(this,"Please enter a valid MAC address", Toast.LENGTH_SHORT).show();
//        }
    }


    protected boolean matchIP(){
        String entered=String.valueOf(s1.getSelectedItem());
        String regexPattern="([0-9A-Fa-f]{2}[:-]?){5}([0-9A-Fa-f]{2})";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(entered);
        return matcher.matches();
   }
}
