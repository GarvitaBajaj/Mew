package recruitment.iiitd.edu.utils;

/**
 * Created by garvitab on 22-09-2015.
 */
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

public class QueryFiltersLatLon implements InputFilter {

	private double min, max;

	public QueryFiltersLatLon(String min, String max) {
		this.min = Double.parseDouble(min);
		this.max = Double.parseDouble(max);
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		try {
			double input;
			String a=dest.toString()+source.toString();
			if(a.startsWith("-")) {
				if (a.length() > 1) {
					String x = a.substring(1);
					input = Double.parseDouble(x);
					if (isInRange(min, max, input))
						return null;
				}
				else{
					return null;
				}
			}else{
				input=Double.parseDouble(a);
				if (isInRange(min, max, input))
					return null;
			}

		} catch (NumberFormatException nfe) {
			LogTimer.blockingDeque.add(System.currentTimeMillis()+" "+ this.getClass().toString()+ " "+nfe.getMessage());
//			FirebaseCrash.logcat(Log.ERROR, "Exception caught", "Collecting data");
//			FirebaseCrash.report(nfe);
 }
		return "";
	}

	private boolean isInRange(double a, double b, double c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
}