package recruitment.iiitd.edu.rabbitmq;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import recruitment.iiitd.edu.mew.SettingsActivity;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;
import recruitment.iiitd.edu.utils.Pair;

/**
 * Created by garvitab on 18-09-2015.
 */
public class RabbitMQConnections {

	private final String exchangeName = "exchange.ps.resquery";
	private final String selectionExchangeName = "exchange.ps.selection";
	private final String exchangeType = "topic";
	private final boolean durable = true;

//	public static int i = 0;

	Thread publishThread;
	Thread subscribeThread;

	public static AtomicBoolean isSubscribing = new AtomicBoolean(true);

	private ConnectionFactory factory;

	public final static String TAG = "Mew";
	private BlockingDeque<Pair<String, String>> queue = new LinkedBlockingDeque< Pair<String, String> >();
//	private static Pair<String,String> lastResourcePair = null;
	private static RabbitMQConnections instance = null;

	private static Context mContext;
	private static String uri;

	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
	String username = sharedPref.getString(SettingsActivity.SETTINGS_USERNAME, "");
	String password = sharedPref.getString(SettingsActivity.SETTINGS_PASSWORD, "");
	String ipaddress = sharedPref.getString(SettingsActivity.SETTINGS_IPADDRESS, "");



	private RabbitMQConnections() {
		registerPreferenceListener();
		uri = "amqp://"+ username +":"+ password +"@"+ ipaddress +":5672/%2f";
		factory = new ConnectionFactory();
		try {
			factory.setAutomaticRecoveryEnabled(true);
			factory.setUri(uri);
			factory.setNetworkRecoveryInterval(10000);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
			e1.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e1.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e1.getMessage());
//			FirebaseCrash.report(e1);
		}

	}

	private void registerPreferenceListener() {
		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(listener);
	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener =
			new SharedPreferences.OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
					if (key.equals(SettingsActivity.SETTINGS_USERNAME)){
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
						String username = sharedPref.getString(SettingsActivity.SETTINGS_USERNAME, "");
						String password = sharedPref.getString(SettingsActivity.SETTINGS_PASSWORD, "");
						String ipaddress = sharedPref.getString(SettingsActivity.SETTINGS_IPADDRESS, "");
						uri = "amqp://"+ username +":"+ password +"@"+ ipaddress +":5672/%2f";
						Log.d(TAG, "Updating URI");
					}
					if (key.equals(SettingsActivity.SETTINGS_IPADDRESS)){
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
						String username = sharedPref.getString(SettingsActivity.SETTINGS_USERNAME, "");
						String password = sharedPref.getString(SettingsActivity.SETTINGS_PASSWORD, "");
						String ipaddress = sharedPref.getString(SettingsActivity.SETTINGS_IPADDRESS, "");
						uri = "amqp://"+ username +":"+ password +"@"+ ipaddress +":5672/%2f";
						Log.d(TAG, "Updating URI");
					}
					if (key.equals(SettingsActivity.SETTINGS_PASSWORD)){
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
						String username = sharedPref.getString(SettingsActivity.SETTINGS_USERNAME, "");
						String password = sharedPref.getString(SettingsActivity.SETTINGS_PASSWORD, "");
						String ipaddress = sharedPref.getString(SettingsActivity.SETTINGS_IPADDRESS, "");
						uri = "amqp://"+ username +":"+ password +"@"+ ipaddress +":5672/%2f";
						Log.d(TAG, "Updating URI");
					}

					updateConnectionFactory();
				}
			};


	private void updateConnectionFactory(){
		factory = new ConnectionFactory();
		try {
			factory.setAutomaticRecoveryEnabled(true);
			factory.setUri(uri);
			factory.setNetworkRecoveryInterval(10000);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
			e1.printStackTrace();
			LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e1.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", e1.getMessage());
//			FirebaseCrash.report(e1);
		}
	}

	public static RabbitMQConnections getInstance(Context context) {
		if(instance == null) {
			mContext = context;
			instance = new RabbitMQConnections();
		}
		return instance;
	}

	public void addMessageToQueue(Map<String,Object> msg, String routing_key){
		System.out.println("RabbitMQ msg: "+new JSONObject(msg).toString());
		Pair<String,String> resourcePair=new Pair<>(new JSONObject(msg).toString(),routing_key);
		queue.add(resourcePair);
//		Log.d("Message Type", msg.get("TYPE").toString());
	}

	public void sendDataUsingAsync(JSONObject msg){
		//call AsyncTask
		RabbitMQData sendData=new RabbitMQData(mContext);
		sendData.execute(msg);
	}


	/***
	 * This thread keeps running in the background
	 * Whenever a new message is added to the queue, it is automatically published to the RabbitMQ Server
	 */
	public void publishToAMQP()
	{

		//new function which receives the message and appends it to the queue
		//TODO Look into using ThreadPool in case message influx is high
		publishThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Connection connection = factory.newConnection();
						Channel ch = connection.createChannel();
						ch.confirmSelect();
						ch.exchangeDeclare(exchangeName, exchangeType, durable);
						Pair<String,String> message = queue.takeFirst();
						String routingKey = message.getRight();
						ch.basicPublish(exchangeName, routingKey , null, message.getLeft().getBytes());
						Log.d("MEW", "Message sent successfully: " + message.getRight() + " Queue Length " + queue.size());
						ch.waitForConfirmsOrDie(30000);
						ch.close();
						connection.close();
//					}
				} catch (InterruptedException e) {
						LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
						e.printStackTrace();
//					FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//					FirebaseCrash.report(e);
				} catch (Exception e) {
					Log.d("MEW", "Connection broken: " + e.getMessage());
					e.printStackTrace();
						LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//					FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//					FirebaseCrash.report(e);
//					try {
//						Thread.sleep(1000); //sleep time reduced to one second for the next try
//					} catch (InterruptedException e1) {
//						FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Exception publishing messages to RabbitMQ");
//						FirebaseCrash.report(e);
//					}
				}
			}
			}
		});
		publishThread.start();
	}


	public void subscribe()
	{
		Log.d(TAG, "started subscriber thread");

		subscribeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Connection connection = factory.newConnection();
						Channel channel = connection.createChannel();
						channel.basicQos(0);
						DeclareOk q = channel.queueDeclare();
						String bindingKey = Constants.DEVICE_ID;
						channel.queueBind(q.getQueue(), selectionExchangeName , bindingKey);

						QueueingConsumer consumer = new QueueingConsumer(channel);
						channel.basicConsume(q.getQueue(), true, consumer);

						// Process deliveries
						while (isSubscribing.get()) {

							QueueingConsumer.Delivery delivery = consumer.nextDelivery();

							String message = new String(delivery.getBody());
							String routingKey = delivery.getEnvelope().getRoutingKey();

							final JSONObject jsonObject=new JSONObject(message);

							if(!isSubscribing.get()){
								if(jsonObject.has("QUERY")){
									break;
								}
							}

							if(jsonObject.has("TYPE") && !jsonObject.has("Query")){//getInt("TYPE")==Constants.MESSAGE_TYPE.DATA.getValue()){
								Thread processData=new Thread() {
									public void run() {
										Random rand = new Random();
										int  n = rand.nextInt(500) + 1;
										Log.e("TYPE","number generated: "+n);
										try {
											Log.e("TYPE","received a new message"+jsonObject.getString("queryNo"));
											File directory;
											File file;
											BufferedWriter writer;
											String queryNo,sensorData;
											queryNo = jsonObject.getString("queryNo");
											sensorData = jsonObject.getString("sensorData");
											directory = new File(new File(Environment.getExternalStorageDirectory() + "/Mew/ReceivedFile/").getPath(), queryNo);
											if (!directory.exists()) {
												directory.mkdirs();
											}
											file = new File(directory, "response" + n + ".csv");
											if (file.exists()) {
												file.delete();
												try {
													file.createNewFile();
													Log.i("new file created", queryNo);
//													i++;
												} catch (IOException e) {
													e.printStackTrace();
													LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//													FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//													FirebaseCrash.report(e);
												}
											}
											writer = null;
											try {
												writer = new BufferedWriter(new FileWriter(file, false));
												writer.write(sensorData);
												writer.flush();

											} catch (IOException e) {
												LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
												e.printStackTrace();
//												FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//												FirebaseCrash.report(e);
											}
											try {
												writer.close();
											} catch (IOException e) {
												LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
												e.printStackTrace();
//												FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//												FirebaseCrash.report(e);
											}
										} catch (Exception e) {
											LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
											e.printStackTrace();
//											FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//											FirebaseCrash.report(e);
										}

									}
								};
								processData.start();
							}
							else {
								Bundle bundle = new Bundle();
								String temp = routingKey + ":" + message;
								bundle.putString("msg", temp);
								Intent localIntent = new Intent(mContext, SubscribeReceiver.class);
								localIntent.setAction(Constants.BROADCAST_ACTION);
								localIntent.putExtra(Constants.AMQP_SUBSCRIBED_MESSAGE, message);
								mContext.sendBroadcast(localIntent);
							}
//							System.out.println("Delivery tag "+delivery.getEnvelope().getDeliveryTag());
							channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
						}
						channel.close();
						connection.close();
					} catch (InterruptedException e) {
						LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
//						FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//						FirebaseCrash.report(e);
//						break;
					}catch (ShutdownSignalException e2){
						LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e2.getMessage());
//						FirebaseCrash.logcat(Log.ERROR, "Exception caught", e2.getMessage() );
//						FirebaseCrash.report(e2);
						e2.getReason();
					}catch (ConnectException e){
						Log.e("Connection Error","Make sure server is up and running");
						LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e.getMessage());
//						FirebaseCrash.logcat(Log.ERROR, "Exception caught",e.getMessage());
//						FirebaseCrash.report(e);
					}catch (Exception e1) {
						Log.d(TAG, "Connection broken: " + e1.getClass().getName());
						e1.printStackTrace();
						LogTimer.blockingDeque.add(System.currentTimeMillis() + ": " + this.getClass().toString() + " : " + e1.getMessage());
//						FirebaseCrash.logcat(Log.ERROR, "Exception caught", e1.getMessage());
//						FirebaseCrash.report(e1);
						try {
							Thread.sleep(4000); //sleep and then try again
						} catch (InterruptedException e) {
							LogTimer.blockingDeque.add(System.currentTimeMillis()+": "+this.getClass().toString()+" : "+e.getMessage());
//							FirebaseCrash.logcat(Log.ERROR, "Exception caught", e.getMessage());
//							FirebaseCrash.report(e);
//							break;
						}
					}
				}
			}
		});
		subscribeThread.start();
	}

}
