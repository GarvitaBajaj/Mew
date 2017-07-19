package algos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONObject;
import database.*;
import org.springframework.stereotype.Component;

import background.MewServerResponseGateway;
import interfaces.MainScreen;
import utils.Provider;
import utils.RandomSingleton;
import utils.SensingQuery;

@Component
public class BatteryGreedy implements Algo {

	public BatteryGreedy() {
	}

	public BatteryGreedy(JSONObject query) {
		this.query=query;
	}

	private static Lock weightLock = new ReentrantLock();
	JSONObject query;

	public void run() {
		System.out.println("USING GREEDY ALGORITHM");
		try {
			
			int minProviders=query.getInt("min");

			String sensorName=query.getString("dataReqd");

			String powerSensor="";

			if(sensorName.equalsIgnoreCase("GPS"))
			{
				powerSensor="GPSPower";
			}
			if(sensorName.equalsIgnoreCase("Accelerometer"))
			{
				powerSensor="AccPower";
			}
			if(sensorName.equalsIgnoreCase("Gyroscope"))
			{
				powerSensor="gyrpower";
			}
			if(sensorName.equalsIgnoreCase("Microphone"))
			{
				powerSensor="MicPower";
			}
			if(sensorName.equalsIgnoreCase("WiFi"))
			{
				powerSensor="WiFiPower";
			}

			String queryID=query.getString("queryNo");

			// check if sufficient providers are available
			String countNodes="select sum(provider=1 and "+powerSensor+" > 0) from nodes;";
			PreparedStatement checkNodes=connect.prepareStatement(countNodes);
			ResultSet nodeCount=checkNodes.executeQuery();
			nodeCount.next();
			int availableCount=nodeCount.getInt(1);
			nodeCount.close();
			checkNodes.close();
			//find providers only if available providers is greater than or equal to minProviders

			if(availableCount>=minProviders) {
				//continue processing 
				List<Provider> allProviders=new ArrayList<Provider>();
				try {
					//if you are waiting for 20 seconds till timeout, you should wait for atleast 21 seconds for a thread to acquire the lock
					if(weightLock.tryLock(21, TimeUnit.SECONDS)) 
					{
						PreparedStatement allProv=connect.prepareStatement("select DeviceID from nodes where ProviderMode=1 and "+powerSensor+">0;");
						ResultSet availableProvs=allProv.executeQuery();
						while(availableProvs.next()){
							allProviders.add(new Provider(availableProvs.getString(1),0, 0,0,0,0, 0, 0, 0, false));
						}
						allProv.close();
						List<String> selectedProviders=new ArrayList<String>();
						
						List<Provider> result=new ArrayList<>();
						// pick the required devices with highest battery levels from the MySQL database  
						String state="select DeviceID from nodes where ProviderMode=1 order by battery desc limit "+minProviders;
						ResultSet valueResult = DatabaseHelper.dbOperation(state);
						while(valueResult.next()){
							//add the provider to the list of selectedProviders
							String device=valueResult.getString(1);
							selectedProviders.add(device);
							result.add(allProviders.stream().filter(p1->p1.getDeviceId().equals(device)).findFirst().get());
						}
						
						System.out.println("All: "+allProviders);
						List<Provider> availableProviders=new ArrayList<>();
						int numProviders = result.size();

						// if count is > 0 add the providers to selected providers list
						if(numProviders > 0) {
							for(Provider p: result) {
								//if device is selected, write it to DeviceSelection.log with last field as 1
								selectedProviders.add(p.deviceId);
								if(!availableProviders.contains(p)) {
									availableProviders.add(p);
								}
							} 
						}
						System.out.println("selected Provider :" + selectedProviders );
						
						//for selected providers, use x=-n in the update rule and send queries 
						for(int i=0;i<selectedProviders.size();i++)
						{
							MewServerResponseGateway test = MewServerResponseGateway.getInstance();
							JSONObject sendQuery=new JSONObject();
							sendQuery.put("Query",query);
							System.out.println(sendQuery.toString());
							test.publishQueryToProviders(sendQuery.toString(), selectedProviders.get(i));
							String insertQueryStr = "insert into queries(queryID, providerID, QueryAllocation) values (?,?,?)";
							PreparedStatement insertQuery=connect.prepareStatement(insertQueryStr);
							insertQuery.setString(1, queryID);
							insertQuery.setString(2, selectedProviders.get(i));
							insertQuery.setString(3, query.getString("selection"));
							insertQuery.executeUpdate();
							insertQuery.close();
							
							//update servicing variable to true
							String setServicing="update nodes set servicing=true where DeviceID=?";
							PreparedStatement set=connect.prepareStatement(setServicing);
							set.setString(1,selectedProviders.get(i));
							set.executeUpdate();
							set.close();
						}

					}
				}catch(Exception e) {
					e.printStackTrace();
				}finally {
					weightLock.unlock();
				}

			}
			else {
				System.out.println("Min providers not available");
				String setServiced="insert into queries(queryID, providerID, QueryJson, serviced) values ('"+queryID+"','unavailable','"+ query.toString()+"',2)";
				PreparedStatement set=connect.prepareStatement(setServiced);
				set.executeUpdate();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


}
