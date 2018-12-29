package recruitment.iiitd.edu.mew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartServiceReceiver extends BroadcastReceiver
{

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
//        {
            Intent i=new Intent(context,HomeScreen.class);
            context.startActivity(i);
//        }
//        else {
//            Log.e(TAG, "onReceive");
//            context.startService(new Intent(context.getApplicationContext(), ExtractParameters.class));
//        }
    }

}
