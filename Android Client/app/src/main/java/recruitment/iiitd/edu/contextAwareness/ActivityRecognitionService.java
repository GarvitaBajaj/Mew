package recruitment.iiitd.edu.contextAwareness;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognitionService extends IntentService {
    public ActivityRecognitionService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognitionService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getMostProbableActivity() );
        }
    }

    private void handleDetectedActivities(DetectedActivity probableActivity) {

        // Get the type
        int activityType = probableActivity.getType();
        /* types:
         * DetectedActivity.IN_VEHICLE
         * DetectedActivity.ON_BICYCLE
         * DetectedActivity.ON_FOOT
         * DetectedActivity.STILL
         * DetectedActivity.UNKNOWN
         * DetectedActivity.TILTING
         */
        Log.e("ACTIVITY",String.valueOf(activityType));
        getApplicationContext().getSharedPreferences("StateValues", Context.MODE_PRIVATE).edit().putInt("CONTEXT",activityType).apply();
    }
}
