package algos;

import algoHelpers.Algo;
import background.MewServerResponseGateway;
import database.DatabaseHelper;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import utils.Constants;
import utils.Provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * In this class, we implement Greedy selection of devices based on their Nicevalues instead of weighted random selection
 * We would like to compare weighted random selection, greedy selection, random selection, and aggregate sensing selection
 * @author garvitab
 *
 */
@Component
public class NVGreedy implements Algo {


public NVGreedy(){

}
    public NVGreedy(JSONObject query) {
        this.query=query;
    }
    private static Lock weightLock = new ReentrantLock();
    JSONObject query;

    public void run() {
        try {
            System.out.println("USING NVGREEDY ALGORITHM");
            double sumNV=0.0;
            double minNV=0.0;
            long startTime=System.currentTimeMillis();
            long nvTime,weightTime, selectionTime;

            int minProviders=query.getInt("min");
            String queryID=query.getString("queryNo");

            String sensorName=query.getString("dataReqd");

            double mPower = 0;
            int colRunning=0,colPower=0;

            String powerSensor="";

            if(sensorName.equalsIgnoreCase("GPS"))
            {
                powerSensor="GPSPower";
                colRunning=9;colPower=8;
            }
            if(sensorName.equalsIgnoreCase("Accelerometer"))
            {
                powerSensor="AccPower";
                colRunning=5;colPower=6;
            }
            if(sensorName.equalsIgnoreCase("Gyroscope"))
            {
                powerSensor="gyrpower";
                colRunning=9;colPower=10;
            }

            // add condition for selecting providers with that sensor available
            String countNodes="select sum(ProviderMode=1 and "+powerSensor+" > 0) from nodes;";
//			System.out.println(countNodes);
            PreparedStatement checkNodes=connect.prepareStatement(countNodes);
            ResultSet nodeCount=checkNodes.executeQuery();
            nodeCount.next();
            int availableCount=nodeCount.getInt(1);
            nodeCount.close();
            checkNodes.close();

            if(availableCount>=minProviders) {

                String maxValues="select max("+powerSensor+"), max(LinkSpeed) from nodes";
                PreparedStatement parameters=connect.prepareStatement(maxValues);
                ResultSet maxInfo = parameters.executeQuery();
                maxInfo.next();
                mPower=maxInfo.getDouble(1);
                int mSpeed=maxInfo.getInt(2);
                maxInfo.close();
                parameters.close();

                //continue processing
                String selection="select * from  nodes where ProviderMode=1 and "+powerSensor+" > 0";
                PreparedStatement selectNodes=connect.prepareStatement(selection);
                ResultSet availableNodes=selectNodes.executeQuery();
//                ResultSetMetaData rsmd = availableNodes.getMetaData();
//                int columnCount = rsmd.getColumnCount();
//
//                for (int i = 1; i <= columnCount; i++ ) {
//                    String name = rsmd.getColumnName(i);
//                    System.out.println(name+ " : "+ String.valueOf(i));
//                }

                List<Provider> allProviders=new ArrayList<Provider>();

                double nv=0.0;
                double W2= Constants.W2;
                double W1=Constants.W1;

                try {
                    if(weightLock.tryLock(21, TimeUnit.SECONDS)) { 	//if you are waiting for 20 seconds till timeout, you should wait for atleast 21 seconds for a thread to acquire the lock

                        Double maxD=0.0;
                        while(availableNodes.next())
                        {
                            //for all available nodes
                            Double querylat=query.getDouble("latitude");
                            Double querylon=query.getDouble("longitude");

                            //calculate the max distance for all the providers - iterate over all values and find the max
                            //get the new location of the node
                            Double dIlat=availableNodes.getDouble(13);			//old latitude
                            Double dIlon=availableNodes.getDouble(14);			//old longitude
                            Double odIlat=availableNodes.getDouble(15);			//new latitude
                            Double odIlon=availableNodes.getDouble(16);			//new longitude

                            //calculate the Euclidean distance of the device from the POI
                            Double distance=Math.sqrt(Math.pow(querylat-dIlat,2)+Math.pow((querylon-dIlon),2));
                            Double oDistance=Math.sqrt(Math.pow(querylat-odIlat, 2)+Math.pow((querylon-odIlon),2));

                            //if condition to update the max distance
                            if(distance>maxD) {
                                maxD=distance;
                            }
                            String device = availableNodes.getString(2);
                            System.out.println(device);

                            //TODO print all values contained in availableNodes and match them with the column numbers in lines 140 and 145 - a change in table schema might have resulted in inconsistencies
                            String valueUpdate = "SELECT nvalue, weight FROM mew.nvalues where DeviceID = ?";
                            LinkedHashMap<String, Object> map=new LinkedHashMap<>();
                            map.put("String", device);
                            ResultSet valueResult = DatabaseHelper.dbSelectOperation( valueUpdate, map);
                            if(valueResult.next()){
                                //Provider(String deviceId, double nValue, double dWeight,double battery,double linkSpeed,
                                //					double power, double oDist, double nDist, int nsensors, boolean servicing)
                                allProviders.add(new Provider(availableNodes.getString(2),valueResult.getDouble(1),valueResult.getDouble(2) ,
                                        availableNodes.getDouble(3), availableNodes.getInt(4),
                                        availableNodes.getDouble(6), oDistance, distance, availableNodes.getInt(11), false ));
                            }
                            else{
                                allProviders.add(new Provider(availableNodes.getString(2),0.0,0.0 ,
                                        availableNodes.getDouble(3), availableNodes.getInt(4),
                                        availableNodes.getDouble(6), oDistance, distance, availableNodes.getInt(11), false ));
                            }
                            valueResult.close();
                        }
                        availableNodes.close();
                        selectNodes.close();

                        for(int i=0;i<allProviders.size();i++) {
                            //check if the provider already has a nice value
                            String command2="select * from nvalues a,nodes b where a.DeviceID = b.DeviceID having a.DeviceID=?";
                            String device = allProviders.get(i).getDeviceId();
                            LinkedHashMap<String, Object> map=new LinkedHashMap<>();
                            map.put("String", device);
                            ResultSet rs2=DatabaseHelper.dbSelectOperation(command2, map);

                            //if not, insert the nice values into the table
                            if(!rs2.next()){
                                try{
                                    List<Provider> result = allProviders.stream().filter(s -> s.deviceId.equals(device)).collect(Collectors.toList());
                                    Provider p = result.get(0);
                                    System.out.println(result);
                                    Double distance=(p.distance);
                                    double distancefactor = W2*20*(1-distance/maxD);
                                    double x=p.getBattery()*(p.getLinkSpeed()/mSpeed);
                                    double y=(p.getPower()*p.getNsensors()/mPower);
                                    nv=(W1)*(x*(1+(p.getServcing()?1:0))/y)+distancefactor;
                                    if(nv<minNV)minNV=nv;

                                    //store these into the nice value table when the entry for this device does not exist - first query after device arrival
                                    String insertNvalue="insert into nvalues(DeviceID, nvalue) values(?,?)";
                                    LinkedHashMap<String, Object> queryMap = new LinkedHashMap<String, Object>();
                                    queryMap.put("String", p.getDeviceId());
                                    queryMap.put("Double", nv);
                                    DatabaseHelper.dbOperation(insertNvalue, queryMap);
//									System.out.println("NV of "+p.getDeviceId()+" is "+nv);
                                    result.get(0).setnValue(nv);
                                    int j = allProviders.indexOf(p);
                                    allProviders.get(j).setnValue(nv);
                                }		catch(Exception e){
                                    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                                }
                            }
                            rs2.close();
                        }


                        //find the min of nvalues
                        ResultSet findMin=DatabaseHelper.dbOperation("select min(nvalue) from nvalues;");
                        findMin.next();
                        minNV=findMin.getDouble(1);
                        findMin.close();

                        final double mNV = minNV;
                        //deep copy of all providers list
                        List<Provider> tempProviderList = new ArrayList<Provider>();
                        for(Provider p: allProviders)
                            tempProviderList.add(p);

                        if(minNV<0) {
                            //update all nice values by adding minnvalue to all the niceValues
                            tempProviderList = allProviders.stream().
                                    map(e -> new Provider(e.deviceId, (e.nValue-mNV + 0.01), e.dWeight ,e.battery,e.linkSpeed,e.power,e.old_distance,e.distance, e.nsensors, e.servicing))
                                    .collect(Collectors.toList());
                            System.out.println("delta shift applied");
                        }

                        sumNV = tempProviderList.stream()
                                .mapToDouble(p -> p.getnValue())
                                .sum();

                        //insert -1 as weights to start afresh
                        DatabaseHelper.dbUpdateOperation("update nvalues set weight=-1");

                        //find the weights of providers
                        for( Provider p : tempProviderList){
                            double nvdata = p.getnValue();
//							System.out.println("NV = "+ nvdata);
                            String insertNvalue="update nvalues set weight=? where DeviceID=?";
                            LinkedHashMap<String, Object> queryMap=new LinkedHashMap<>();
                            double weight = nvdata/sumNV;
                            queryMap.put("Double",weight);
                            queryMap.put("String",p.getDeviceId());
                            int j = allProviders.indexOf(p);
                            allProviders.get(j).setdWeight(weight);
                            DatabaseHelper.dbOperation(insertNvalue, queryMap);
                        }

                        //allProviders now contains the nvalues before delta shift and weights calculated after delta shift

//                        weightTime=System.currentTimeMillis()-startTime;

                        //use greedy approach to select providers
                        List<String> selectedProviders=new ArrayList<String>();
                        List<Provider> availableProviders=new ArrayList<>();
                        double rand=0;
                        int sProviders = 0;
                        double rankFactor = 0.0;

                        allProviders.sort(null);
                        double[] acc = {0};

                        List<Provider> all_Providers_weight_acc=allProviders.stream().sorted(Comparator.comparing(Provider::getdWeight)).collect(Collectors.toList());
                        List<Provider> allProviders_acc = all_Providers_weight_acc.stream().peek(e -> acc[0] += e.dWeight)
                                .map(e -> new Provider(e.deviceId, e.nValue, acc[0],e.battery,e.linkSpeed,e.power,e.old_distance,e.distance, e.nsensors, e.servicing))
                                .collect(Collectors.toList());

                        {
                            //pick up the last minProviders from the list of providers sorted in ascending order of weights (or nice values)
                            List<Provider> result = all_Providers_weight_acc.subList(allProviders.size()-minProviders, allProviders.size());// = allProviders_acc.stream().filter(s -> s.dWeight > randF ).collect(Collectors.toList());

                            int numProviders = result.size();

                            // if count is > 0 add the providers to selected providers list
                            if(numProviders > 0) {
                                int selProviders = 0;
                                for(Provider p: result) {
                                    selectedProviders.add(p.deviceId);
                                    if(!availableProviders.contains(p)) {
                                        availableProviders.add(p);
                                        selProviders++;
                                    }
                                }
                                sProviders += selProviders;
                            }
                        }


                        //if provider is not selected, use update rule with x = 1+A(tau)
                        for(Provider p : allProviders) {
                            if(!selectedProviders.contains(p.deviceId)) {
                                int rank = allProviders_acc.indexOf(p);
                                rankFactor = (allProviders_acc.size()- rank)/((double)allProviders_acc.size());
                                double dFactor = (W2)*20*((p.getOld_distance()-p.getDistance())/maxD);
                                nv=p.getnValue() + (rankFactor * (W1*(1+(p.servicing?1:0))*p.getBattery()*(p.getLinkSpeed()/mSpeed)	/(p.getPower()/mPower))+dFactor);

                                p.setnValue(nv);
                                if(nv<minNV)minNV=nv;

                                String update="update nvalues set nvalue=? where DeviceID=?";
                                LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
                                queryMap.put("Double", nv);
                                queryMap.put("String", p.getDeviceId());
                                DatabaseHelper.dbOperation(update, queryMap);
                            }
                        }
                        System.out.println("Update rule applied");

                        System.out.println("selected Provider :" + selectedProviders );


                        //update values and send queries
                        for(int i=0;i<selectedProviders.size();i++)
                        {

                            final int j=i;
                            MewServerResponseGateway test = MewServerResponseGateway.getInstance();
                            JSONObject sendQuery=new JSONObject();
                            sendQuery.put("Query",query);
                            test.publishQueryToProviders(sendQuery.toString(), selectedProviders.get(i));

                            String insertQueryStr = "insert into queries(queryID, providerID, QueryAllocation, QueryJSON) values (?,?,?,?)";
                            PreparedStatement insertQuery=connect.prepareStatement(insertQueryStr);
                            insertQuery.setString(1, queryID);
                            insertQuery.setString(2, selectedProviders.get(i));
                            insertQuery.setString(3, query.getString("selection"));
                            insertQuery.setString(4,query.toString());
                            insertQuery.executeUpdate();
                            insertQuery.close();

                            //add code to decrease the nice value here if the provider has been selected
                            //update nvalues table with new nvalues
                            List<Provider> result = allProviders.stream().filter(s -> s.deviceId.equals(selectedProviders.get(j))).collect(Collectors.toList());
                            Provider p=result.get(0);
                            int rank = allProviders_acc.indexOf(p);
                            rankFactor = (allProviders_acc.size()- rank)/((double)allProviders_acc.size());
                            double dFactor = (W2)*20*((p.getOld_distance()-p.getDistance())/maxD);
                            nv=p.getnValue() - 	(rankFactor * (W1*(p.getBattery()*(p.getLinkSpeed()/mSpeed)*p.getNsensors())/(p.getPower()/mPower)+dFactor));
                            p.setnValue(nv);
                            if(nv<minNV)minNV=nv;
                            String update="update nvalues set nvalue=? where DeviceID=?";
                            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<String, Object>();
                            queryMap.put("Double", nv);
                            queryMap.put("String", p.getDeviceId());
                            DatabaseHelper.dbOperation(update, queryMap);

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
                System.out.println("Min providers not available");
                String selectionType=query.getString("selection");

                String setServiced="insert into queries(queryID, providerID, QueryAllocation,QueryJSON, serviced) values ('"+queryID+"','unavailable','"+selectionType+"',"+query.toString()+"'"+"',2)";
                PreparedStatement set=connect.prepareStatement(setServiced);
                set.executeUpdate();
                set.close();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}

