package recruitment.iiitd.edu.mew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Map;

import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

	public int speed;
	
    @SuppressWarnings("unchecked")
	@Override
    public void onReceive(final Context context, final Intent intent) {

		NetworkUtil networkUtil = new NetworkUtil();
    	speed= networkUtil.getSpeed(context);
        SharedPreferences sharedpreferences = ExtractParameters.sharedpreferences;
        Editor editor = sharedpreferences.edit();
		editor.putInt("LINKSPEED", speed);
		editor.commit();
		System.out.println(sharedpreferences.getAll());
		Map<String, Object> states=new HashMap<String,Object>();
		states.put("TYPE", Constants.MESSAGE_TYPE.INFO.getValue());
		states.put("STATE", sharedpreferences.getAll());
//		System.out.println("This is a broadcast receiver and might be causing the problem");
		RabbitMQConnections publishResource= RabbitMQConnections.getInstance(context);
		publishResource.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);

    }
    
    public NetworkChangeReceiver() {
    	speed=0;
	}
    

}