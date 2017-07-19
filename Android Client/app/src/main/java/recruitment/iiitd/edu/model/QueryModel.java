package recruitment.iiitd.edu.model;

import android.provider.BaseColumns;

import java.io.Serializable;

/**
 * Created by apurv on 10/1/2015.
 */
public final class QueryModel implements BaseColumns, Serializable {

    private static final long serialVersionUID = 8692497235318058617L;
    private int _id;
    private String sensorName;
    private long startTime , endTime;
    private String routingKey;
    private String queryNo;
    private int processed;

    public QueryModel(){

    }

    public QueryModel(String sensorName, long startTime, long endTime, String routingKey, String queryNo){
        this.sensorName = sensorName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.routingKey = routingKey;
        this.queryNo = queryNo;
        this.processed = 0;
    }

    public void setId(int id){
        this._id = id;
    }
    public int getId(){
        return this._id;
    }

    public void setSensorName(String sensorName){
        this.sensorName = sensorName;
    }
    public String getSensorName(){
        return this.sensorName;
    }

    public void setStartTime(long startTime){
        this.startTime = startTime;
    }
    public long getStartTime(){
        return  this.startTime;
    }

    public void setEndTime(long endTime){
        this.endTime = endTime;
    }
    public long getEndTime(){
        return  this.endTime;
    }

    public void setRoutingKey(String routingKey){
        this.routingKey = routingKey;
    }
    public String getRoutingKey(){
        return this.routingKey;
    }

    public void setQueryNo(String queryNo){
        this.queryNo = queryNo;
    }
    public String getQueryNo(){
        return this.queryNo;
    }

    public void setProcessed(int processed){
        this.processed = processed;
    }
    public int getProcessed(){
        return  this.processed;
    }


}
