package recruitment.iiitd.edu.sensing;

import android.content.Context;
import android.util.Log;

//import com.google.firebase.crash.FirebaseCrash;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.LogTimer;

/**
 * Created by garvitab on 22-09-2015.
 */
public class PublishSensorData {

	@SuppressWarnings("unchecked")
	public static void sendAsMessage(Context context,String f,String queryNo, String sensorName, String requesterID) throws JSONException {
		switch(sensorName) {
			case ("Accelerometer"):
			case("Gyroscope"):
			case("WiFi"):
			case("GPS"):
				Log.i("QueryNo in publishData", queryNo);
				StringBuilder data = new StringBuilder();
				CSVReader reader;
				try {
					Log.e("TYPE","Sending file: "+f);
					reader = new CSVReader(new FileReader(f));
					String[] nextLine;
					while ((nextLine = reader.readNext()) != null) {
						int j = 0, k = nextLine.length;
						for (int i = 0; i < k; i++) {
							data.append(nextLine[ i ]);
							if (j != k - 1) {
								data.append(",");
								j++;
							}
						}
						data.append("\n");
					}
					JSONObject servicedData = new JSONObject();
					try {
						servicedData.put("TYPE", Constants.MESSAGE_TYPE.DATA.getValue());
						servicedData.put("sensorName", sensorName);
						servicedData.put("sensorData", data.toString());
						servicedData.put("requesterID",requesterID);
						servicedData.put("queryNo",queryNo);
						System.out.println("JSON object created...entering task in background");
						Log.e("DATA",servicedData.toString());
						RabbitMQConnections publishResource= RabbitMQConnections.getInstance(context);
						publishResource.sendDataUsingAsync(servicedData);

					} catch (JSONException e) {
						e.printStackTrace();
//						LogTimer.blockingDeque.add(System.currentTimeMillis()+" : "+context.getClass().toString()+": "+e.getMessage());
//						FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Publishing data");
//						FirebaseCrash.report(e);
					}
				} catch (Exception e) {
					e.printStackTrace();
//					LogTimer.blockingDeque.add(System.currentTimeMillis() + " : " + context.getClass().toString() + ": " + e.getMessage());
//					FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Publishing data");
//					FirebaseCrash.report(e);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					Log.e("DATA","Thread interrupted");
				}
				break;
			case ("Microphone"):

//				try {
//					StringBuilder sb = new StringBuilder();
//					Base64 encodingByte = new Base64();
//					byte[] bytesEncoded = encodingByte.encode(FileUtils.readFileToByteArray(f));
//					String encodedFile = new String(bytesEncoded);
////					System.out.println("Trying to publish");
//					JSONObject dataPacket = new JSONObject();
//					dataPacket.put("queryNo", queryNo);
//					JSONObject servicedData = new JSONObject();
//					try {
//						servicedData.put("sensorName", sensorName);
//						servicedData.put("sensorData", encodedFile);
//						servicedData.put("requesterID", requesterID);
//						System.out.println(sb.toString());
//						dataPacket.put("data", servicedData);
//					} catch (JSONException e) {
//						e.printStackTrace();
//						LogTimer.blockingDeque.add(System.currentTimeMillis() + " : " + context.getClass().toString() + ": " + e.getMessage());
////						FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Publishing data");
////						FirebaseCrash.report(e);
//					}
//				} catch (IOException e) {
//					LogTimer.blockingDeque.add(System.currentTimeMillis()+" : "+context.getClass().toString()+": "+e.getMessage());
////					FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Publishing data");
////					FirebaseCrash.report(e);
//					e.printStackTrace();
//				}
		}
	}
}
