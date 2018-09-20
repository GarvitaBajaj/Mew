package recruitment.iiitd.edu.contextAwareness;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import recruitment.iiitd.edu.utils.Constants;

/**
 * Created by garvitab on 03-12-2016.
 */
public class WiFiState {

    public static boolean checkIfWiFi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connManager.getActiveNetworkInfo();

        if ((null != ni) && (ni.isConnected()) && (ni.getType() == ConnectivityManager.TYPE_WIFI)) {
            //TODO execute the query only if device is connected to WiFi
            Log.d(Constants.TAG, "WiFi connected");
            return true;
        }
        return false;
    }
}
