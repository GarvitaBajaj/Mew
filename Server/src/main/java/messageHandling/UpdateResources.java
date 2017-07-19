package messageHandling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import interfaces.MainScreen;

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

		double blevel, accpower = 0, gyrpower=0, gpspower=0, micpower=0, wifipower=0,latitude, longitude;
		boolean gpsenabled,servicing;
		int lspeed;
		String noderef;
		int sensors=0;		//number of other sensors present on the device

		try {
			JSONObject state=(JSONObject)resource.get("STATE");
			blevel=(state.getDouble("BATTERY"));
			lspeed=state.getInt("LINKSPEED");
			gpsenabled=state.getBoolean("GPSRunning");
			noderef=(state.getString("DEVICEID"));
			latitude=(state.getDouble("LATITUDE"));
			longitude=state.getDouble("LONGITUDE");
			System.out.println("\nA node sent its parameters "+noderef);			

			try{
				try {
					accpower=(state.getDouble("ACCPOWER"));
					gyrpower=(state.getDouble("GYRPOWER"));
					gpspower=(state.getDouble("GPSPOWER"));
					micpower=state.getDouble("MICPOWER");
					wifipower=state.getDouble("WIFIPOWER");
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
				}catch(JSONException e) {
					System.out.println("This is not the first info packet from this device");
				}
				if((Double.compare(latitude, 200.0)==0 && Double.compare(longitude, 200.0)==0)){

					String command="insert into nodes(DeviceID, Battery, LinkSpeed, AccRunning, AccPower, GPSRunning,GPSPower,GyrPower,SensorsAvailable, micpower,wifipower) values (?,?,?,?,?,?,?,?,?,?,?)";
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
				}
				else{

					String command="insert into nodes(DeviceID, Battery, LinkSpeed, AccRunning, AccPower, GPSRunning,GPSPower,GyrPower,SensorsAvailable,old_lat,old_lon,micpower,wifipower) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
				}
				preparedStatement.executeUpdate();	
				preparedStatement.close();

			}catch(MySQLIntegrityConstraintViolationException intexc)
			{
				//			System.out.println("Exception caught in writing to SQL: key already exists");
				try{
					if((Double.compare(latitude, 200.0)==0 && Double.compare(longitude, 200.0)==0)){
						String command="UPDATE nodes SET Battery=?, LinkSpeed=? where DeviceID=?";
						preparedStatement=connect.prepareStatement(command);
						preparedStatement.setDouble(1, blevel);
						preparedStatement.setInt(2, lspeed);
						preparedStatement.setString(3, noderef);
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

						String command="UPDATE nodes SET Battery=?, LinkSpeed=?, old_lat=?,old_lon=?,new_lat=?,new_lon=? where DeviceID=?";
						preparedStatement=connect.prepareStatement(command);
						preparedStatement.setDouble(1, blevel);
						preparedStatement.setInt(2, lspeed);
						preparedStatement.setDouble(3,old_new_lat);
						preparedStatement.setDouble(4, old_new_lon);
						preparedStatement.setDouble(5, latitude);
						preparedStatement.setDouble(6,longitude);
						preparedStatement.setString(7, noderef);
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
