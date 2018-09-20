//package recruitment.iiitd.edu.contextAwareness;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import com.google.android.gms.awareness.fence.FenceState;
//import com.google.android.gms.awareness.fence.FenceState;
//
///**
// * Created by garvitab on 01-12-2016.
// */
//public class FenceReceiver extends BroadcastReceiver {
//
//		private static final String TAG = "FenceReceiver";
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// Get the fence state
//			FenceState fenceState = FenceState.extract(intent);
//
//			switch (fenceState.getCurrentState()) {
//				case FenceState.TRUE:
//					//TODO user is in the requested location, execute the query, save the location also
//					Log.d(TAG, "User is in location");
//					break;
//				case FenceState.FALSE:
//					Log.i(TAG, "User is not in location");
//					break;
//				case FenceState.UNKNOWN:
//					Log.i(TAG, "User is doing something unknown");
//					break;
//			}
//		}
//	}
//
//
