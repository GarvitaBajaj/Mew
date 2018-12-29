package recruitment.iiitd.edu.contextAwareness;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by garvitab on 05-12-2016.
 */
public class ChargingState {

	public static int checkChargingState(Context context){
		Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		if(plugged==BatteryManager.BATTERY_PLUGGED_AC || plugged==BatteryManager.BATTERY_PLUGGED_USB){
			Log.d("charging state", "Plugged in");
			return 1;
		}
		return 0;
	}
}
