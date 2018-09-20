package beans;

import algoHelpers.Algo;
import algoHelpers.AlgoFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TreeMap;

@Component
public class Scheduler {
	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;

	public static TreeMap<Long, JSONObject> queryQueue = new TreeMap<Long, JSONObject>();

	Long maxTime=Long.MIN_VALUE;
	Long servicePollTime;


	public void schedule() {
		System.out.println("Entered scheduler");
		if(!queryQueue.isEmpty()){
			Long startTime = queryQueue.firstKey();
			if(startTime>maxTime){
				maxTime=startTime;
			}
			JSONObject query = queryQueue.get(startTime);
			Algo algo;
			startTime -= 60000;
			Timestamp stamp = new Timestamp(startTime);
			Date date = new Date(stamp.getTime());
			System.out.println("Scheduling Query to Time " + date);
			String type=query.getString("selection");
			System.out.println("Type: "+type);
			algo=AlgoFactory.getRequestedAlgo(type, query);
			threadPoolTaskScheduler.schedule(algo, date);
			queryQueue.remove(startTime+60000);
		}
	}
}
