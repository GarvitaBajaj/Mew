package recruitment.iiitd.edu.mew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import recruitment.iiitd.edu.rabbitmq.RabbitMQConnections;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

	public int speed;
	public String cellIDs;
    @SuppressWarnings("unchecked")
	@Override
    public void onReceive(final Context context, final Intent intent) {
        SharedPreferences sharedpreferences = ExtractParameters.sharedpreferences;
        Editor editor = sharedpreferences.edit();
        ExtractParameters.wm.startScan();
        if (intent.getAction() == android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
			StringBuilder sb = new StringBuilder();
			if(ExtractParameters.wm.isWifiEnabled()) {
				java.util.List<android.net.wifi.ScanResult> apList = ExtractParameters.wm.getScanResults();
				Set<ScanResult> setAPs = new HashSet<>(apList);
				for (ScanResult result : setAPs) {
//					String bssid = result.BSSID;
					sb.append(result.BSSID + ",");
				}
			}else{sb.append("");}
			editor.putString("WiFiAPs",sb.toString());
        }
		NetworkUtil networkUtil = new NetworkUtil();
    	speed= networkUtil.getSpeed(context);
    	cellIDs=networkUtil.getCellIds(context);
    	editor.putString("MTOWERS",cellIDs);
		editor.putInt("LINKSPEED", speed);
		editor.commit();
		System.out.println(sharedpreferences.getAll());
		Map<String, Object> states=new HashMap<String,Object>();
		states.put("TYPE", Constants.MESSAGE_TYPE.INFO.getValue());
		states.put("STATE", sharedpreferences.getAll());
		RabbitMQConnections publishResource= RabbitMQConnections.getInstance(context);
		publishResource.addMessageToQueue(states, Constants.RESOURCE_ROUTING_KEY);

    }
    
    public NetworkChangeReceiver() {
    	speed=0;
	}
}