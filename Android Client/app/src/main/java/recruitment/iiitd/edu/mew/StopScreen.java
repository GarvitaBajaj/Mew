package recruitment.iiitd.edu.mew;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by garvitab on 18-01-2017.
 */
public class StopScreen extends BroadcastReceiver {
	ComponentName compName;

	@Override
	public void onReceive(Context context, Intent intent) {
		if(HomeScreen.mWakeLock.isHeld()) {
			HomeScreen.mWakeLock.release();
			Log.d("WAKE LOCK RELEASED","true");
		}
		if(HomeScreen.deviceManger==null) {
			HomeScreen.deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		}
		compName=new ComponentName(context,MyAdmin.class);
		boolean active=HomeScreen.deviceManger.isAdminActive(compName);Log.d("ADMIN: ",String.valueOf(active));
		if(active)
			HomeScreen.deviceManger.lockNow();
	}
}
