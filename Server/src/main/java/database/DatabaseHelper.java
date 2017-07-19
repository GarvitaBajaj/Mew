package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import interfaces.MainScreen;

public class DatabaseHelper {

	static Connection connect = null;

	/**
	 * this is for executing select statement with no parameter
	 * @param query: the SQL command to be executed
	 * @return
	 */
	public static synchronized ResultSet dbOperation(String query)
	{
		connect = MainScreen.connect;
		ResultSet resultSet = null;
		try {
			resultSet = connect.prepareStatement(query).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultSet;

	}

	/***
	 * to execute update statements with no parameters
	 * @param query
	 * @return
	 */
	public static synchronized boolean dbUpdateOperation(String query)
	{
		connect = MainScreen.connect;
		PreparedStatement s;
		try {
			s=connect.prepareStatement(query);
			s.executeUpdate();
			s.close();
		} catch(MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
			System.out.println("Executed twice");
		}catch (SQLException e) {
			e.printStackTrace();
			return false;
		}		
		return true;

	}


	/**
	 * this is for executing select statement with parameters
	 * @param query: the SQL command to be executed
	 * @return
	 */
	public static synchronized ResultSet dbSelectOperation(String query, LinkedHashMap<String, Object> parameters)
	{
		connect = MainScreen.connect;

		ResultSet resultSet = null;
		try {
			PreparedStatement command=connect.prepareStatement(query);
			int i = 1;
			for(Map.Entry<String, Object> map : parameters.entrySet()){
				String key = map.getKey();

				if(key.equalsIgnoreCase("String")){
					command.setString(i++, (String)map.getValue());
				}
				else if(key.equalsIgnoreCase("Double")){
					command.setDouble(i++, (Double)map.getValue());
				}
				else {
					System.out.println("Unknown parameter type");
				}
			}
			resultSet = command.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return resultSet;

	}

	/**
	 * for executing update statement with parameter values
	 * @param query: the SQL command to be executed
	 * @param queryMap: the hashmap containing parameter type and value
	 * @return
	 */
	public static synchronized boolean dbOperation(String query, LinkedHashMap<String,Object> queryMap)
	{
		connect = MainScreen.connect;
		try {	

			PreparedStatement queryStmt = connect.prepareStatement(query);
			int i = 1;
			for(Map.Entry<String, Object> map : queryMap.entrySet()){
				String key = map.getKey();

				if(key.equalsIgnoreCase("String")){
					queryStmt.setString(i++, (String)map.getValue());
				}
				else if(key.equalsIgnoreCase("Double")){
					queryStmt.setDouble(i++, (Double)map.getValue());
				}
				else {
					System.out.println("Unknown parameter type");
				}
			}
			queryStmt.executeUpdate();
			queryStmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}

}
