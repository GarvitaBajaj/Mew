package algos;

import algoHelpers.Algo;
import background.MewServerResponseGateway;
import interfaces.MainScreen;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import utils.Provider;
import utils.RandomSingleton;
import utils.SensingQuery;

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

@Component
public class Broadcast implements Algo{

	
	public Broadcast() {
	}

	public Broadcast(JSONObject query) {
		this.query=query;
		connect=MainScreen.connect;
	}

	private static Lock weightLock = new ReentrantLock();
	Connection connect=null;
	JSONObject query;
	RandomSingleton random = null;
	int duration=0;
	String choice="";

	public void run() {
		random = RandomSingleton.getInstance();
		Map<String, Integer> diffValues=new HashMap<>();
		System.out.println("USING BROADCAST ALGORITHM");
		try {
			
//			int minProviders=query.getInt("min");

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
				powerSensor="GyrPower";
			}
			if(sensorName.equalsIgnoreCase("Microphone"))
			{
				powerSensor="MicPower";
			}
			if(sensorName.equalsIgnoreCase("WiFi"))
			{
				powerSensor="WiFiPower";
			}

			//fetch the query parameters and create an object of the SensingQuery class
			//aggregate sensing time measured in minutes
			//currently, each automated task has a duration of 5 minutes
			String queryID=query.getString("queryNo");
			long fromTime=query.getLong("fromTime");
			long toTime=query.getLong("toTime");
			duration=(int)(toTime-fromTime)/60000;
			SensingQuery thisQuery=new SensingQuery();
			thisQuery.setEndTime(toTime);
			thisQuery.setStartTime(fromTime);
			thisQuery.setQueryID(queryID);

			// check if sufficient providers are available
			String countNodes="select sum(ProviderMode=1 and "+powerSensor+" > 0) from nodes;";
			PreparedStatement checkNodes=connect.prepareStatement(countNodes);
			ResultSet nodeCount=checkNodes.executeQuery();
			nodeCount.next();
			int availableCount=nodeCount.getInt(1);
			nodeCount.close();
			checkNodes.close();
			//find providers only if available providers is greater than or equal to minProviders

			if(availableCount>=1) {
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
						
						//select all available providers
						for(Provider p: allProviders) {
							//if device is selected, write it to DeviceSelection.log with last field as 1
							selectedProviders.add(p.deviceId);
						}
						
						System.out.println("All: "+allProviders);
						
						System.out.println("selected Provider :" + selectedProviders );
						
						//for selected providers, use x=-n in the update rule and send queries 
						for(int i=0;i<selectedProviders.size();i++)
						{
							MewServerResponseGateway test = MewServerResponseGateway.getInstance();
							JSONObject sendQuery=new JSONObject();
							sendQuery.put("Query",query);
							System.out.println(sendQuery.toString());
							test.publishQueryToProviders(sendQuery.toString(), selectedProviders.get(i));
							String insertQueryStr = "insert into queries(queryID, providerID, QueryAllocation, QueryJSON) values (?,?,?,?)";
							PreparedStatement insertQuery=connect.prepareStatement(insertQueryStr);
							insertQuery.setString(1, queryID);
							insertQuery.setString(2, selectedProviders.get(i));
							insertQuery.setString(3, query.getString("selection"));
							insertQuery.setString(4,query.toString());
							insertQuery.executeUpdate();
							insertQuery.close();
							
							//update servicing variable to true
							String setServicing="update nodes set servicingTask=true where DeviceID=?";
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
				System.out.println("No providers are available");
				String selectionType=query.getString("selection");
				String setServiced="insert into queries(queryID, providerID, QueryAllocation,QueryJSON, serviced) values ('"+queryID+"','unavailable','"+selectionType+"',"+query.toString()+"'"+"',2)";
				System.out.println(setServiced);
				PreparedStatement set=connect.prepareStatement(setServiced);
				set.executeUpdate();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
