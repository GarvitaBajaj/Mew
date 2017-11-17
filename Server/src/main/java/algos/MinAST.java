package algos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.json.JSONObject;
import utils.*;
import background.*;
import org.springframework.stereotype.Component;

import algoHelpers.Algo;
import interfaces.MainScreen;

@Component
public class MinAST implements Algo{

	public MinAST() {
	}

	public MinAST(JSONObject query) {
		this.query=query;
	}

	int duration;
	private static Lock weightLock = new ReentrantLock();
	JSONObject query;

	public void run() {
		Map<String, Integer> diffValues=new HashMap<>();
		System.out.println("USING BASELINE ALGORITHM");

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
						List<Provider> availableProviders=allProviders;
						List<String> selectedProviders=new ArrayList<String>();
						/**
						 * **********************************************
						 * select providers based on the INFOCOM 2014 paper
						 * **********************************************
						 */
						System.out.println("All: "+allProviders);
						List<Provider> result=new ArrayList<>();
						List<ProviderAS> providersAS=new ArrayList<>();
						//go through all the providers to find which one to select
						//each iteration will select one device for this query
						for(Provider p: availableProviders){
							String device1 = p.getDeviceId();
							//find queries that are yet to be executed by this device-> assigned; not yet finished execution-> not deleted
							String queries_in_db="select Distinct QueryJSON from mew.queries_audit where ProviderID=? and Actions=? and QueryID NOT IN (Select QueryID from mew.queries_audit where ProviderID=? and Actions=? and serviced=1);";
							PreparedStatement abc=connect.prepareStatement(queries_in_db);
							abc.setString(1, device1);
							abc.setString(2, "insert");
							abc.setString(3, device1);
							abc.setString(4, "delete");
							ResultSet assigned=abc.executeQuery();

							//list of startTimes of queries assigned to the provider
							List<Long> temp1=new ArrayList<>();
							try{
								while(assigned.next()){
									JSONObject assigned_query=new JSONObject(assigned.getString(1));
									temp1.add(assigned_query.getLong("fromTime"));
								}
							}catch(Exception e){
								e.printStackTrace();
							}
							ProviderAS a=new ProviderAS();
							a.setProvider(p.getDeviceId());
							int old=calculateAggSensingTime(temp1);
							a.setOldSensingtime(old);
							//							System.out.println(device1+": "+old);
							temp1.add(thisQuery.getStartTime());	
							int newS=calculateAggSensingTime(temp1);
							a.setNewSensingtime(newS);
							//							System.out.println(device1+": "+newS);
							assigned.close();
							abc.close();

							//calculate the difference between the old and new sensing times for this device
							int diff=newS-old;
							diffValues.put(device1, diff);
							a.setDifftime(diff);
							providersAS.add(a);
						}

						int selProviders=0;		
						//check if there is any device with zero diff in aggregate sensing time
						//shouldn't happen in our case as queries are sequential and of equal duration
						Iterator<Entry<String, Integer>> it = diffValues.entrySet().iterator();
						while(it.hasNext()) {
							//if time diff is zero, select that provider
							//remove the devices from the map to be considered that have already been selected
							Map.Entry<String,Integer> pair = (Entry<String, Integer>)it.next();
							if((Integer)pair.getValue()==0){
								it.remove();
								selProviders++;
								result.add(allProviders.stream().filter(p1->p1.getDeviceId().equals(pair.getKey())).findFirst().get());
								availableProviders.remove(allProviders.stream().filter(p1->p1.getDeviceId().equals(pair.getKey())).findFirst().get());
								providersAS.remove(providersAS.stream().filter(p1->p1.getProvider().equals(pair.getKey())).findFirst().get());
							}
						}

						List<ProviderAS> providers1=providersAS.stream().sorted(Comparator.comparing(ProviderAS::getNewSensingtime).thenComparing(ProviderAS::getDifftime)).collect(Collectors.toList());
						int ide=0;
						while(selProviders<minProviders){
							//find the minimum aggregate sensing time from the map
							//iterate through the list of providers for this
							String found=providers1.get(ide).getProvider();
							result.add(allProviders.stream().filter(p1->p1.getDeviceId().equals(found)).findFirst().get());
							selProviders++;
							ide++;
						}
						System.out.println("SIZE OF ALL: "+ result.size());
						// if more providers than required, then randomly select minProviders from the list
						if(selProviders >= minProviders) {
							for(int i=0;i<minProviders;i++) {
								Provider p = result.get(i);
								selectedProviders.add(p.deviceId);
							}
						}

						System.out.println("selected Provider :" + selectedProviders );
						System.out.println("All: "+result);

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

	/**func to return the names of providers with min sensing times
	 * 
	 */
	public static List<Object> getKeysFromValue(Map<?, ?> hm, Object value){
		List <Object>list = new ArrayList<Object>();
		for(Object o:hm.keySet()){
			if(hm.get(o).equals(value)) {
				list.add(o);
			}
		}
		return list;
	}


	/**
	 * func to calculate the aggregate sensing time of a device when a new task is allocated to it
	 * @param temp: an array containing the start times of all tasks assigned to a device
	 * @return
	 */
	private int calculateAggSensingTime(List<Long> temp1) {
		int aggSenTime;
		//if empty, then return 0
		if(temp1.isEmpty())
			aggSenTime=0;

		//if there are more tasks, then check if they are overlapping
		//first, sort this list in ascending order: now we know the start times in sequential order
		else {
			Collections.sort(temp1);
			aggSenTime=duration;
			for(int i=0;i<temp1.size()-1;i++){
				//check if consecutive tasks are covered
				if(temp1.get(i+1)==temp1.get(i)){
					//do nothing
				}
				//pick adjacent tasks and check if the second task starts before the first ends (overlapping)
				else if(temp1.get(i+1)<(temp1.get(i)+(duration*60000))){
					//convert the difference in startTimes to minutes
					aggSenTime+=((temp1.get(i+1)/60000)-(temp1.get(i)/60000));
				}
				//if non overlapping, then add the entire duration to the aggregate sensing time
				else{
					aggSenTime+=duration;
				}
			}
		}
		//		System.out.println("Aggregate sensing time is "+aggSenTime);
		return aggSenTime;
	}

	
}
