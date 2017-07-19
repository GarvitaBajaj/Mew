package algos;

import java.sql.Connection;

import org.json.JSONObject;

import interfaces.MainScreen;
import utils.RandomSingleton;

public interface Algo extends Runnable{
	
	public static Connection connect=MainScreen.connect;
	public static RandomSingleton random = RandomSingleton.getInstance();
	
	public void run();
}
