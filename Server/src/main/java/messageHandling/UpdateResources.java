package messageHandling;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import interfaces.MainScreen;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**should be called only to update the dynamic parameter values
 * 
 * @author Garvita Bajaj
 *
 */
public class UpdateResources extends Thread {

	Connection connect=null;
	JSONObject resource;

	public UpdateResources(JSONObject resource) {
		this.resource=resource;
		connect=MainScreen.connect;
	}

	public void run() {

		PreparedStatement preparedStatement = null;

		//	System.out.println("\n\nReceived info message from a node");

		double blevel, accpower = 0, gyrpower=0, gpspower=0, micpower=0, wifipower=0, barpower=0, ppgpower=0,latitude, longitude;
		boolean gpsenabled,servicing,barrunning = false, ppgrunning=false;
		int lspeed, context=4,activities=-1;
		String noderef,wifiaps,mtowers;
		long logtime;
		int sensors=0;		//number of other sensors present on the device

		try {
			JSONObject state=(JSONObject)resource.get("STATE");
			blevel=(state.getDouble("BATTERY"));
			lspeed=state.getInt("LINKSPEED");
			gpsenabled=state.getBoolean("GPSRunning");
			noderef=(state.getString("DEVICEID"));
			latitude=(state.getDouble("LATITUDE"));
			longitude=state.getDouble("LONGITUDE");
			System.out.println("\nA node sent its parameters "+state);
			wifiaps=state.getString("WiFiAPs");
			mtowers=state.getString("MTOWERS");
			logtime=state.getLong("LOGTIME");
			if(wifiaps.length()>=1000){
				wifiaps=wifiaps.substring(0,999);
			}
			try{
				try {
					accpower=(state.getDouble("ACCPOWER"));
					gyrpower=(state.getDouble("GYRPOWER"));
					gpspower=(state.getDouble("GPSPOWER"));
					micpower=state.getDouble("MICPOWER");
					wifipower=state.getDouble("WIFIPOWER");
					barpower=state.getDouble("BARPOWER");
					ppgpower=state.getDouble("PPGPOWER");
					activities=state.getInt("ACTIVITIES");
					context=state.getInt("CONTEXT");
					barrunning=state.getBoolean("BarRunning");
//					ppgrunning=state.getBoolean("PpgRunning");
					
					//			System.out.println("Value of mic power is: "+ micpower);
					if(accpower>0)
						sensors++;
					if(gpspower>0)
						sensors++;
					if(gyrpower>0)
						sensors++;
					if(micpower>0)
						sensors++;
					if(wifipower>0)
						sensors++;
					if(barpower>0)
						sensors++;
					if(ppgpower>0)
						sensors++;
				}catch(JSONException e) {
					System.out.println("This is not the first info packet from this device");
					e.printStackTrace();
				}
				if((Double.compare(latitude, 200.0)==0 && Double.compare(longitude, 200.0)==0)){

					String command="insert into nodes(DeviceID, Battery, LinkSpeed, AccRunning, AccPower, GPSRunning,GPSPower,GyrPower,SensorsAvailable, micpower,wifipower,barrunning,barpower,ppgrunning,ppgpower,ActivitiesRunning,Context, providerMode,WiFiAPs,MTowers, LogTime) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					preparedStatement=connect.prepareStatement(command);
					preparedStatement.setString(1, noderef);
					preparedStatement.setDouble(2, blevel);
					preparedStatement.setInt(3, lspeed);
					preparedStatement.setBoolean(4,true);
					preparedStatement.setDouble(5, accpower);				
					preparedStatement.setBoolean(6, gpsenabled);
					preparedStatement.setDouble(7,gpspower);
					preparedStatement.setDouble(8, gyrpower);
					preparedStatement.setInt(9, sensors);
					preparedStatement.setDouble(10,micpower);
					preparedStatement.setDouble(11,wifipower);
					preparedStatement.setBoolean(12,barrunning);
					preparedStatement.setDouble(13,barpower);
					preparedStatement.setBoolean(14,ppgrunning);
					preparedStatement.setDouble(15,ppgpower);
					preparedStatement.setInt(16,activities);
					preparedStatement.setInt(17,context);
					preparedStatement.setInt(18,1);
					preparedStatement.setString(19,wifiaps);
					preparedStatement.setString(20,mtowers);
					preparedStatement.setLong(21,logtime);
				}
				else{

					String command="insert into nodes( DeviceID, Battery, LinkSpeed, AccRunning, AccPower, GPSRunning,GPSPower,GyrPower,SensorsAvailable,old_lat,old_lon,micpower,wifipower,barrunning,barpower,ppgrunning,ppgpower,ActivitiesRunning,Context,providerMode,WiFiAPs,MTowers, LogTime) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					preparedStatement=connect.prepareStatement(command);
					preparedStatement.setString(1, noderef);
					preparedStatement.setDouble(2, blevel);
					preparedStatement.setInt(3, lspeed);
					preparedStatement.setBoolean(4,true);
					preparedStatement.setDouble(5, accpower);				
					preparedStatement.setBoolean(6, gpsenabled);
					preparedStatement.setDouble(7,gpspower);
					preparedStatement.setDouble(8, gyrpower);
					preparedStatement.setInt(9, sensors);
					preparedStatement.setDouble(10,latitude);
					preparedStatement.setDouble(11,longitude);
					preparedStatement.setDouble(12, micpower);
					preparedStatement.setDouble(13, wifipower);
					preparedStatement.setBoolean(14,barrunning);
					preparedStatement.setDouble(15,barpower);
					preparedStatement.setBoolean(16,ppgrunning);
					preparedStatement.setDouble(17,ppgpower);
					preparedStatement.setInt(18,activities);
					preparedStatement.setInt(19,context);
					preparedStatement.setInt(20,1);
					preparedStatement.setString(21,wifiaps);
					preparedStatement.setString(22,mtowers);
					preparedStatement.setLong(23,logtime);
				}
				preparedStatement.executeUpdate();	
				preparedStatement.close();

			}catch(MySQLIntegrityConstraintViolationException intexc)
			{
				//			System.out.println("Exception caught in writing to SQL: key already exists");
				try{
					if((Double.compare(latitude, 200.0)==0 && Double.compare(longitude, 200.0)==0)){
						String command="UPDATE nodes SET Battery=?, LinkSpeed=?,ActivitiesRunning=?,Context=?,WiFiAPs=?,MTowers=?,LogTime=? where DeviceID=?";
						preparedStatement=connect.prepareStatement(command);
						preparedStatement.setDouble(1, blevel);
						preparedStatement.setInt(2, lspeed);
						preparedStatement.setInt(3,activities);
						preparedStatement.setInt(4,context);
						preparedStatement.setString(5,wifiaps);
						preparedStatement.setString(6,mtowers);
						preparedStatement.setLong(7,logtime);
						preparedStatement.setString(8, noderef);
						preparedStatement.executeUpdate();
						preparedStatement.close();
						System.out.println("Values updated");
					}else{
						String getLocation="Select new_lat, new_lon from nodes where DeviceID=?";

						PreparedStatement abc=connect.prepareStatement(getLocation);
						abc.setString(1, noderef);
						ResultSet location=abc.executeQuery();
						Double old_new_lat,old_new_lon;

						/**
						 * location values not fetched from the existing table
						 */

						location.next();
						old_new_lat=location.getDouble(1);
						old_new_lon=location.getDouble(2);

						String command="UPDATE nodes SET Battery=?, LinkSpeed=?,ActivitiesRunning=?,Context=?, old_lat=?,old_lon=?,new_lat=?,new_lon=?,WiFiAPs=?,MTowers=?,LogTime=? where DeviceID=?";
						System.out.println(logtime);
						preparedStatement=connect.prepareStatement(command);
						preparedStatement.setDouble(1, blevel);
						preparedStatement.setInt(2, lspeed);
						preparedStatement.setInt(3,activities);
						preparedStatement.setInt(4,context);
						preparedStatement.setDouble(5,old_new_lat);
						preparedStatement.setDouble(6, old_new_lon);
						preparedStatement.setDouble(7, latitude);
						preparedStatement.setDouble(8,longitude);
						preparedStatement.setString(9,wifiaps);
						preparedStatement.setString(10,mtowers);
						preparedStatement.setLong(11,logtime);
						preparedStatement.setString(12, noderef);
						preparedStatement.executeUpdate();
						preparedStatement.close();
						System.out.println("Values updated");
					}
				}catch (SQLException sqlexc)
				{
					System.out.println("Another exception in SQL command operation");
					sqlexc.printStackTrace();		
				}
				catch(Exception exc){
					System.out.println("Some unindentified exception occured");
					exc.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}catch(JSONException e){
			System.out.println("JSON exception: ");
			e.printStackTrace();
		}
	}
}
