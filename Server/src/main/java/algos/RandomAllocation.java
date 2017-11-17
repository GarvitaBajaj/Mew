package algos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import background.*;
import org.json.JSONObject;
import utils.Provider;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.deser.Deserializers.Base;

import algoHelpers.Algo;
import algoHelpers.BaseMethods;

@Component
public class RandomAllocation implements Algo{

	public RandomAllocation() {
	}

	public RandomAllocation(JSONObject query) {
		this.query=query;
	}

	JSONObject query;

	
	public void run() {
		try {
			System.out.println("USING RANDOM SELECTION");

			int minProviders=query.getInt("min");
			String queryID=query.getString("queryNo");

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

			// add condition for selecting providers with that sensor available
			PreparedStatement checkNodes=connect.prepareStatement("select sum(providerMode=1 and "+powerSensor+">0) from nodes;");
			ResultSet nodeCount=checkNodes.executeQuery();
			nodeCount.next();
			int availableCount=nodeCount.getInt(1);
			System.out.println("Available count: "+availableCount);
			nodeCount.close();
			checkNodes.close();

			if(availableCount>=minProviders) {

				List<String> selectedProviders=new ArrayList<String>();
				List<Provider> allProviders=new ArrayList<Provider>();
				List<Provider> availableProviders=new ArrayList<>();

				PreparedStatement allProv=connect.prepareStatement("select DeviceID from nodes where ProviderMode=1 and "+powerSensor+">0;");
				ResultSet availableProvs=allProv.executeQuery();
				while(availableProvs.next()){
					allProviders.add(new Provider(availableProvs.getString(1),0, 0,0,0,0, 0, 0, 0, false));
				}
				allProv.close();
				
				List<Provider> result = pickNRandom(allProviders, minProviders);
				if(result.size()!=minProviders)
					System.err.println("Some problem selecting providers randomly");
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

				//update values and send queries 
				for(int i=0;i<selectedProviders.size();i++)
				{
					BaseMethods.sendQuery(query, selectedProviders.get(i));
					//add code to decrease the nice value here if the provider has been selected
				}
			}else {
				System.out.println("Min providers not available");
				String selectionType=query.getString("selection");
				String setServiced="insert into queries(queryID, providerID, QueryAllocation, serviced) values ('"+queryID+"','unavailable','"+selectionType+"',2)";
				PreparedStatement set=connect.prepareStatement(setServiced);
				set.executeUpdate();
				set.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * using Collections.shuffle() to produce unbiased permutations that are equally likely
	 * @param lst: The list of all available providers for the requested query
	 * @param n: The number of providers requested
	 * @return: The list of randomly selected providers
	 */
	public static List<Provider> pickNRandom(List<Provider> lst, int n) {
		List<Provider> copy = new ArrayList<Provider>(lst);
		Collections.shuffle(copy);
		return copy.subList(0, n);
	}


}
