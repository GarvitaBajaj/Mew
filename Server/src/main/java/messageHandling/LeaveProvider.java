package messageHandling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import interfaces.MainScreen;

public class LeaveProvider extends Thread{

	Connection connect=null;
	JSONObject query;

	public LeaveProvider(JSONObject query) {
			this.query=query;
			this.connect=MainScreen.connect;
	}
	
	public void run() {

		String command="UPDATE nodes SET providerMode=? where DeviceID=?";
		String command2="delete from nvalues where DeviceID=?";

		try {
			PreparedStatement preparedStatement = connect.prepareStatement(command);
			preparedStatement.setBoolean(1, false);
			preparedStatement.setString(2, query.getString("NODE"));
			preparedStatement.executeUpdate();
			System.out.println("Nodes table updated");
			
			PreparedStatement preparedStatement2 = connect.prepareStatement(command2);
			preparedStatement2.setString(1, query.getString("NODE"));
			preparedStatement2.executeUpdate();
			System.out.println("Deleted from nvalues table");
			preparedStatement.close();
			preparedStatement2.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

	}
}
