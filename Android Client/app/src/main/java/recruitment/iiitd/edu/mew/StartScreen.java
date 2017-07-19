package recruitment.iiitd.edu.mew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by garvitab on 18-01-2017.
 */
public class StartScreen extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(HomeScreen.mWakeLock==null)
			HomeScreen.mWakeLock=HomeScreen.mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_DIM_WAKE_LOCK,"wakelock");
		if(HomeScreen.mWakeLock.isHeld())
			HomeScreen.mWakeLock.release();
		HomeScreen.mWakeLock = HomeScreen.mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
		if(!HomeScreen.mWakeLock.isHeld())
			HomeScreen.mWakeLock.acquire();
		Log.v("SCREEN LOCK", "ON!");

	}
}
