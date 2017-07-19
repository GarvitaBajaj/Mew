package recruitment.iiitd.edu.rabbitmq;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import recruitment.iiitd.edu.mew.SettingsActivity;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;

/**
 * Created by garvitab on 23-09-2015.
 */
public class RabbitMQData extends AsyncTask<JSONObject, Void, Void> {

	private ConnectionFactory factory;
	String username,password,ipaddress;
	private final String selectionExchangeName = "exchange.ps.selection";
	private final String exchangeType = "topic";
	private final boolean durable = true;

	public RabbitMQData(Context mContext){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		username = sharedPref.getString(SettingsActivity.SETTINGS_USERNAME, "");
		password = sharedPref.getString(SettingsActivity.SETTINGS_PASSWORD, "");
		ipaddress = sharedPref.getString(SettingsActivity.SETTINGS_IPADDRESS, "");
	}

	@Override
	protected Void doInBackground(JSONObject... strings) {
		String uri = "amqp://"+ username +":"+ password +"@"+ ipaddress +":5672/%2f";

		factory = new ConnectionFactory();
		try {
			factory.setAutomaticRecoveryEnabled(false);
			factory.setUri(uri);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
			e1.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e1.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e1.getMessage());
//			FirebaseCrash.report(e1);
		}
		JSONObject msg=strings[0];

		Log.d(Constants.TAG,"JSON Message: "+msg.toString());

		try {
			Connection connection = factory.newConnection();
			Channel ch = connection.createChannel();
			ch.confirmSelect();
			Log.e("TYPE",selectionExchangeName+": "+exchangeType+": "+durable);
			ch.exchangeDeclare(selectionExchangeName, exchangeType , durable);
			//if the routing key does not exist, AMQP will silently drop the messages without waiting for an acknowledgement from the broker
			//see http://forum.spring.io/forum/spring-projects/integration/amqp/130602-rabbittemplate-send-not-throwing-exception-on-non-existing-exchange-and-routing-key
			String routingKey = msg.getString("requesterID");
			Log.d(Constants.TAG," Requester id : "+routingKey);
			ch.basicPublish(selectionExchangeName, routingKey, null, msg.toString().getBytes());
			ch.waitForConfirmsOrDie(30000);
			ch.close();
			connection.close();
		} catch (Exception e) {
			Log.d(Constants.TAG,"Exception caught");
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught",e.getMessage());
//			FirebaseCrash.report(e);
//		} catch (JSONException e) {
//			e.printStackTrace();
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
////			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
////			FirebaseCrash.report(e);
//		} catch (IOException e) {
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
//			e.printStackTrace();
////			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
////			FirebaseCrash.report(e);
//		} catch (TimeoutException e) {
//			LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
//			e.printStackTrace();
////			FirebaseCrash.report(e);
		}

		return null;
	}
}
