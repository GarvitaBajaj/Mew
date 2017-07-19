package algos;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import background.MewServerResponseGateway;
import interfaces.MainScreen;

public class BaseMethods {
	
	private static BaseMethods baseMethod = null ;
	
	private BaseMethods() {
	}
	
	public static BaseMethods getInstance() {
		if(baseMethod == null) {
			return new BaseMethods();
		}
		return baseMethod;
	}
	
	public static void sendQuery(JSONObject query,String deviceID){
		MewServerResponseGateway test = MewServerResponseGateway.getInstance();
		JSONObject sendQuery=new JSONObject();
		sendQuery.put("Query",query);
		System.out.println(sendQuery.toString());
//		test.sendResponse(sendQuery.toString(), selectedProviders.get(i));
		test.publishQueryToProviders(sendQuery.toString(), deviceID);
		String insertQueryStr = "insert into queries(queryID, providerID, QueryAllocation) values (?,?,?)";
		PreparedStatement insertQuery;
		try {
			insertQuery = MainScreen.connect.prepareStatement(insertQueryStr);
			insertQuery.setString(1, query.getString("queryNo"));
			insertQuery.setString(2, deviceID);
			insertQuery.setString(3, query.getString("selection"));
			insertQuery.executeUpdate();
			insertQuery.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
