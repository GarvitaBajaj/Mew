package recruitment.iiitd.edu.interfaces;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

import java.util.List;

/**
 * Created by Manan Wason on 30/11/16.
 */

public interface WifiStateListener {

    List<WifiConfiguration> getConfiguredNetworks();

    WifiInfo getConnectionInfo();

    DhcpInfo getDhcpInfo();

    List<ScanResult> getScanResults();

    boolean is5GHzBandSupported();

    boolean isDeviceToApRttSupported();

    boolean isEnhancedPowerReportingSupported();

    boolean isP2pSupported();

    boolean isPreferredNetworkOffloadSupported();

    boolean isScanAlwaysAvailable();

    boolean isTdlsSupported();

    boolean isWifiEnabled();

    void start(Context context, int samplingRate, String queryNo,String file);

    void stop();


}
