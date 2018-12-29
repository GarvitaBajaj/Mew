
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Query {

    String sensorName = null;
    long fromTime;
    long toTime;
    long expiryTime;
    int min, max;
    double latitude = Double.NaN;
    double longitude = Double.NaN;
    int frequency = -1;
    Long queryTime;

    public String getType() {
        return selection;
    }

    public void setType(String selection) {
        this.selection = selection;
    }

    String selection, queryNo=null;

    /**
     * @return the requester ID for the query
     */
    public String getRequesterID() {
        return requesterID;
    }

    public void setRequesterID(String requesterID) {
        this.requesterID = requesterID;
    }

    String requesterID=null;

    public Query() {
        queryTime = System.currentTimeMillis();
    }

    /**
     * @return the min
     */
    public int getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public int getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * S
     *
     * @return the sensorName
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * @param sensorName the sensorName to set
     */
    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    /**
     * @return the queryNo
     */
    public String getQueryNo() {
        return this.requesterID+queryTime.toString();
    }

    /**
     * @param
     */
//    public void setQueryNo(){
//        this.queryNo=this.requesterID+queryTime.toString();
//    }
    /**
     * @return the fromTime
     */
    public long getFromTime() {
        return fromTime;
    }

    /**
     * @param fromTime the fromTime to set
     */
    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    /**
     * @return the toTime
     */
    public long getToTime() {
        return toTime;
    }

    /**
     * @param toTime the toTime to set
     */
    public void setToTime(long toTime) {
        this.toTime = toTime;
    }

    /**
     * @return the expiryTime
     */
    public long getExpiryTime() {
        return expiryTime;
    }

    /**
     * @param expiryTime the expiryTime to set
     */
    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @SuppressWarnings("unchecked")
    public static void sendQueryToServer(JSONObject jsonQuery) throws InterruptedException, JSONException {
        System.out.println("Trying to send query");
        Map<String, Object> querymsg = new HashMap<String, Object>();
//		querymsg.put("TYPE", Constants.MESSAGE_TYPE.QUERY.getValue());
        querymsg.put("Query", jsonQuery);

        System.out.println("Query : "+ jsonQuery.toString());
//        RabbitMQConnections publishResource= RabbitMQConnections.getInstance(context);
//        publishResource.addMessageToQueue(querymsg, Constants.QUERY_ROUTING_KEY);
    }

    public static JSONObject generateJSONQuery(Query query) {
        JSONObject jsonquery = new JSONObject();
        try {
            jsonquery.put("requesterID", query.getRequesterID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            jsonquery.put("selection",query.getType());
        }catch (JSONException e){
            e.printStackTrace();
        }
        try {
            jsonquery.put("min", query.getMin());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("max", query.getMax());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("fromTime", query.getFromTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("toTime", query.getToTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("expiryTime", query.getExpiryTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("latitude", query.getLatitude());
        } catch (JSONException e) {
            System.out.println("null value in latitude...inserting null");
            try {
                jsonquery.put("latitude", JSONObject.NULL);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        try {
            jsonquery.put("longitude", query.getLongitude());
        } catch (JSONException e) {
            try {
                System.out.println("null value in longitude");
                jsonquery.put("longitude", JSONObject.NULL);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        try {
            jsonquery.put("frequency", query.getFrequency() == -1 ? JSONObject.NULL : query.getFrequency());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("dataReqd", query.getSensorName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonquery.put("queryNo", query.getQueryNo());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonquery;
    }
}
