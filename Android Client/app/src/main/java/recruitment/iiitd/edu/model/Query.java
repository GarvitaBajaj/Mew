package recruitment.iiitd.edu.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.LogTimer;

public class Query {

	public static final String PREFS_NAME = "StateValues";
	SharedPreferences preferences;
	String sensorName = null;
	long fromTime;
	long toTime;
	long expiryTime;

	public String getSelection() {
		return selection;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	String selection;
	List<String> sensors = null;
	int min, max;
	double latitude = Double.NaN;
	double longitude = Double.NaN;
	int frequency = -1;
	static Context context;
	Long queryTime;

	public Query(Context context) {
		queryTime = System.currentTimeMillis();
		Query.context = context;
		preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	/**
	 * @return the min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @return the sensors
	 */
	public List<String> getSensors() {
		return sensors;
	}

	/**
	 * S
	 *
	 * @return the sensorName
	 */
	public String getSensorName() {
		return sensorName;
	}

	/**
	 * @param sensorName the sensorName to set
	 */
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	/**
	 * @return the queryNo
	 */
	public String getQueryNo() {
		return Constants.DEVICE_ID + queryTime.toString();
	}

	/**
	 * @return the fromTime
	 */
	public long getFromTime() {
		return fromTime;
	}

	/**
	 * @param fromTime the fromTime to set
	 */
	public void setFromTime(long fromTime) {
		this.fromTime = fromTime;
	}

	/**
	 * @return the toTime
	 */
	public long getToTime() {
		return toTime;
	}

	/**
	 * @param toTime the toTime to set
	 */
	public void setToTime(long toTime) {
		this.toTime = toTime;
	}

	/**
	 * @return the expiryTime
	 */
	public long getExpiryTime() {
		return expiryTime;
	}

	/**
	 * @param expiryTime the expiryTime to set
	 */
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		//System.out.print(latitude);
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the frequency
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@SuppressWarnings("unchecked")
	public static void sendQueryToServer(JSONObject jsonQuery, Context context) throws InterruptedException, JSONException {
		Log.d(Constants.TAG, "Trying to send query");
		Map<String, Object> querymsg = new HashMap<String, Object>();
//		querymsg.put"(TYPE", Constants.MESSAGE_TYPE.QUERY.getValue());
		querymsg.put("Query", jsonQuery);

		Log.d("Query",jsonQuery.toString());
		RabbitMQConnections publishResource= RabbitMQConnections.getInstance(context);
		publishResource.addMessageToQueue(querymsg, Constants.QUERY_ROUTING_KEY);

		Log.d(Constants.TAG,"Query sent to the server");

	}

	public static JSONObject generateJSONQuery(Query query) {
		JSONObject jsonquery = new JSONObject();
		try {
			jsonquery.put("selection",query.getSelection());
			jsonquery.put("requesterID", Constants.DEVICE_ID);
		} catch (JSONException e) {
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("min", query.getMin());
		} catch (JSONException e) {
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("max", query.getMax());
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("fromTime", query.getFromTime());
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("toTime", query.getToTime());
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("expiryTime", query.getExpiryTime());
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("latitude", query.getLatitude());
		} catch (JSONException e) {
			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+context.getClass().toString()+" : "+e.getMessage());
			System.out.println("null value in latitude...inserting null");
			try {
				jsonquery.put("latitude", JSONObject.NULL);
			} catch (JSONException e1) {
				e1.printStackTrace();
				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//				FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//				FirebaseCrash.report(e);
			}
		}
		try {
			jsonquery.put("longitude", query.getLongitude());
		} catch (JSONException e) {
			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+context.getClass().toString()+" : "+e.getMessage());
			try {
				System.out.println("null value in longitude");
				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
				jsonquery.put("longitude", JSONObject.NULL);
			} catch (JSONException e1) {
				e1.printStackTrace();
				LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//				FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//				FirebaseCrash.report(e);
			}
		}
		try {
			jsonquery.put("frequency", query.getFrequency() == -1 ? JSONObject.NULL : query.getFrequency());
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}
		try {
			jsonquery.put("dataReqd", query.getSensorName());
		} catch (JSONException e) {
			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+context.getClass().toString()+" : "+e.getMessage());
			e.printStackTrace();
		}
		try {
			jsonquery.put("queryNo", query.getQueryNo());
		} catch (JSONException e) {
			e.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + context.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "JSON Exception in generating query");
//			FirebaseCrash.report(e);
		}

		return jsonquery;
	}
}