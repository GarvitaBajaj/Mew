package recruitment.iiitd.edu.rabbitmq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import recruitment.iiitd.edu.model.DatabaseHelper;
import recruitment.iiitd.edu.model.QueryModel;
//import recruitment.iiitd.edu.sensing.AccReadings;
import recruitment.iiitd.edu.sensing.SensorReadings;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;

public class SubscribeReceiver extends BroadcastReceiver {

	private final String TAG = this.getClass().getName();
	static int i = 0;
	public static AtomicInteger runningServices=new AtomicInteger(0);

//	HashMap<String,JSONObject > queryList = new HashMap<>();

	@Override
	public void onReceive(Context context, Intent intent) {
		// an Intent broadcast.
		String msg = intent.getStringExtra(Constants.AMQP_SUBSCRIBED_MESSAGE);
		System.out.println(msg.toString());

		try {
			JSONObject queryRcvd = new JSONObject(msg);
			if (queryRcvd.has("Query")) {
				processQuery(context,queryRcvd);

			} else {
				Log.d(Constants.TAG, "NO QUERY IN THE RECEIVED MESSAGE !!!");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//			FirebaseCrash.report(e);
		}
	}

	private void processQuery(Context context, JSONObject queryRcvd) {

		//add query to a new task queue
		try {
			JSONObject jsonQuery = new JSONObject(String.valueOf(queryRcvd.getJSONObject("Query")));
			QueryModel query = getQueryFromJson(jsonQuery);
			String sensorName=query.getSensorName();
			if(query == null) {
				Log.e(TAG, "Query Parse Exception");
				return;
			}

			DatabaseHelper db = new DatabaseHelper(context);
			int queryId = (int) db.addQuery(query);
			Log.d(TAG, "Query Saved to db with id "+ queryId);
			db.closeDb();


			if (query.getStartTime() > System.currentTimeMillis()) {

//				if (query.getSensorName().equalsIgnoreCase("accelerometer")) {
					SensorReadings.processRequest(context, query.getQueryNo(), Constants.PROCESS_DATA_REQUEST,sensorName,null);
//				}
			} else {
				System.out.println(System.currentTimeMillis() + " > " + query.getStartTime());
				System.out.println("Can't collect data for a past time frame. Dropping query# " + query.getQueryNo());

				//send some message to the server informing about this issue
				Map<String,Object> errorQuery=new HashMap<>();
				errorQuery.put("TYPE",Constants.MESSAGE_TYPE.NOPROCESSING.getValue());
				errorQuery.put("QUERY",query.getQueryNo());
				errorQuery.put("NODE",Constants.DEVICE_ID);
				RabbitMQConnections publishResource= RabbitMQConnections.getInstance(context);
				publishResource.addMessageToQueue(errorQuery, Constants.RESOURCE_ROUTING_KEY);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//			FirebaseCrash.report(e);
		}
	}

	private QueryModel getQueryFromJson(JSONObject query){

		try {
			long start = query.getLong("fromTime");
			long end = query.getLong("toTime");
			String sensor = query.getString("dataReqd");
			String routingKey = query.getString("requesterID");
			String queryNo = query.getString("queryNo");
			QueryModel queryModel = new QueryModel(sensor, start, end, routingKey, queryNo);
			return queryModel;
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//			FirebaseCrash.report(e);
		}
		return  null;
	}
}
