package recruitment.iiitd.edu.mew;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import recruitment.iiitd.edu.model.Query;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;
import recruitment.iiitd.edu.utils.QueryFiltersLatLon;


public class QueryForm extends ActionBarActivity {

	Spinner sensors;
	EditText ed1,ed2,ed3,ed4,ed5,ed6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query_form);
		addItemsOnSpinner();
		ed1=(EditText)findViewById(R.id.editText1);		//minimum #sensors
		ed2=(EditText)findViewById(R.id.editText2);		//maximum #sensors
		ed3=(EditText)findViewById(R.id.editText);		//latitude
		ed4=(EditText)findViewById(R.id.editText3);		//longitude
		ed5=(EditText)findViewById(R.id.editText4);		//duration
		ed6=(EditText)findViewById(R.id.editText5); 	//selection criteria
		ed3.setFilters(new InputFilter[]{ new QueryFiltersLatLon("0", "90.0")});
		ed4.setFilters(new InputFilter[]{new QueryFiltersLatLon("0", "180.0")});

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_query_form, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public Query createQuery()
	{
		Query query = new Query(this);
		try {
			long fromTime = System.currentTimeMillis() + Constants.DELAY_QUERY_PROCESSING;
			query.setSensorName(sensors.getSelectedItem().toString());
			query.setMin(Integer.parseInt(ed1.getText().toString()));
			query.setMax(Integer.parseInt(ed2.getText().toString()));
			query.setLatitude(Double.parseDouble(ed3.getText().toString()));
			query.setLongitude(Double.parseDouble(ed4.getText().toString()));
			query.setFromTime(fromTime);
			query.setSelection(ed6.getText().toString());
			query.setToTime(fromTime + Integer.parseInt(ed5.getText().toString()) * 1000);
			query.setExpiryTime(System.currentTimeMillis() + (15 * 60 * 1000));
		}catch (Exception e){
			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
			query = null;
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Generating query");
//			FirebaseCrash.report(e);
		}
		return query;
	}

	public void addItemsOnSpinner() {

		sensors = (Spinner) findViewById(R.id.spinner1);
		String[] sensor_names={"Accelerometer","Microphone","Gyroscope","GPS","Camera","WiFi"};
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,sensor_names);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sensors.setAdapter(dataAdapter);
	}
	public void publishQuery(View v) throws IOException, InterruptedException, JSONException
	{
		Query q=createQuery();
		if(q==(null)){
			Toast.makeText(this,"Please ensure that field values are valid",Toast.LENGTH_SHORT).show();
		}
		else {
			JSONObject jsonQuery = Query.generateJSONQuery(q);
			Query.sendQueryToServer(jsonQuery, this);
			Log.d(Constants.TAG, "Query Generated: " + jsonQuery.toString());
			Toast.makeText(this,"Your query has been registered",Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onBackPressed(){
		super.onBackPressed();
	}
}
