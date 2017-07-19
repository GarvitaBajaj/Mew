package utils;

public class SensingQuery {

	String QueryID;
	long startTime;
	long endTime;
	
	public String getQueryID() {
		return QueryID;
	}
	public void setQueryID(String queryID) {
		QueryID = queryID;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
}

