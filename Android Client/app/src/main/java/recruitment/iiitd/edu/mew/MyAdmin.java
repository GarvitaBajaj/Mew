package recruitment.iiitd.edu.mew;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by garvitab on 19-01-2017.
 */
public class MyAdmin extends DeviceAdminReceiver {


	static SharedPreferences getSamplePreferences(Context context) {
		return context.getSharedPreferences( DeviceAdminReceiver.class.getName(), 0);
	}

	void showToast(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		showToast(context, "Sample Device Admin: enabled");
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		return "This is an optional message to warn the user about disabling.";
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		showToast(context, "Sample Device Admin: disabled");
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		showToast(context, "Sample Device Admin: pw changed");
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		showToast(context, "Sample Device Admin: pw failed");
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		showToast(context, "Sample Device Admin: pw succeeded");
	}

}
