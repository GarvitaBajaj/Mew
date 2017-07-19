package messageHandling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import interfaces.MainScreen;

public class LeaveListener extends Thread{
	
	Connection connect=null;
	JSONObject query;

	public LeaveListener(JSONObject query) {
			this.query=query;
			this.connect=MainScreen.connect;
	}

	public void run() {

		//delete that entry from the nodes table and nvalues table
		String command1="delete from nodes where DeviceID=?";
		String command2="delete from nvalues where DeviceID=?";

		try {
			PreparedStatement preparedStatement = connect.prepareStatement(command1);
			preparedStatement.setString(1, query.getString("NODE"));
			preparedStatement.executeUpdate();
			System.out.println("Deleted from nodes table");

			PreparedStatement preparedStatement2 = connect.prepareStatement(command2);
			preparedStatement2.setString(1, query.getString("NODE"));
			preparedStatement2.executeUpdate();
			preparedStatement.close();
			preparedStatement2.close();
			System.out.println("Deleted from nvalues table");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
