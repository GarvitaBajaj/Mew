package recruitment.iiitd.edu.contextAwareness;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import recruitment.iiitd.edu.mew.HomeScreen;

/**
 * Created by garvitab on 01-12-2016.
 */
public class UserActivity extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UserActivity(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //...
        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostProbableActivity
                    = result.getMostProbableActivity();
//
//            // Get the confidence % (probability)
//            int confidence = mostProbableActivity.getConfidence();

            // Get the type
            int activityType = mostProbableActivity.getType();
            /* types:
             * DetectedActivity.IN_VEHICLE
             * DetectedActivity.ON_BICYCLE
             * DetectedActivity.ON_FOOT
             * DetectedActivity.STILL
             * DetectedActivity.UNKNOWN
             * DetectedActivity.TILTING
             */
            Log.e("ACTIVITY",String.valueOf(activityType));
            getApplicationContext().getSharedPreferences("StateValues", Context.MODE_PRIVATE).edit().putInt("ACTIVITIES",activityType).apply();
        }
    }
//    	IN_VEHICLE(0),ON_BICYCLE(1) ,ON_FOOT(2) ,STILL(3) ,UNKNOWN(4),TILTING (5),WALKING (6),RUNNING (7);

//    private int value;
//    private static int activity;// = false;

//    UserActivity(int value) {
//        this.value = value;
//    }
//
//    public int getValue() {
//        return value;
//    }
//
//    public static int identifyActivity(final int requestedActivity) {
//        Awareness.SnapshotApi.getDetectedActivity(HomeScreen.mGoogleApiClient)
//                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
//                    @Override
//                    public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
//                        if (!detectedActivityResult.getStatus().isSuccess()) {
//                            Log.e("Activity", "Could not get the current activity.");
//                            return;
//                        }
//                        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
//                        List<DetectedActivity> probableActivity = ar.getProbableActivities();
//                      activity=ar.getMostProbableActivity().getType();
////                        for (DetectedActivity d : probableActivity) {
////                            Log.d("Activity", "Activity detected: " + d.getType() + " , Activity confidence: " + d.getConfidence());
////                        }
////                        //match the activity requested to the list of probable activities
////                        if (probableActivity.contains(requestedActivity)) {
////                            isActivity = true;
////                        }
//                    }
//                });
//        return activity;
//    }
}
