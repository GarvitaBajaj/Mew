//package recruitment.iiitd.edu.sensing;
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.content.Context;
//import android.util.Log;
//
///**
// * An {@link IntentService} subclass for handling asynchronous task requests in
// * a service on a separate handler thread.
// * <p/>
// * TODO: Customize class - update intent actions, extra parameters and static
// * helper methods.
// */
//public class SensorCollectionService extends IntentService {
//
//    public final String TAG = this.getClass().getName();
//    // TODO: Rename actions, choose action names that describe tasks that this
//
//    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
//    private static final String ACTION_DATA_REQUEST = "recruitment.iiitd.edu.sensing.action.DATA_REQUEST";
//    private static final String ACTION_FOO = "recruitment.iiitd.edu.sensing.action.FOO";
//    private static final String ACTION_BAZ = "recruitment.iiitd.edu.sensing.action.BAZ";
//
//    // TODO: Rename parameters
//    private static final String EXTRA_PARAM1 = "recruitment.iiitd.edu.sensing.extra.PARAM1";
//    private static final String EXTRA_PARAM2 = "recruitment.iiitd.edu.sensing.extra.PARAM2";
//
//    /**
//     * Starts this service to perform action Foo with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//
//    public static void newDataCollectionRequest(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, SensorCollectionService.class);
//        intent.setAction(ACTION_FOO);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }
//
//    public static void startActionFoo(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, SensorCollectionService.class);
//        intent.setAction(ACTION_FOO);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }
//
//    /**
//     * Starts this service to perform action Baz with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, SensorCollectionService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }
//
//    public SensorCollectionService() {
//        super("SensorCollectionService");
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.v(TAG, "Sensor Data Collection Service Created");
//    }
//
//    @Override
//    public int onStartCommand (Intent intent, int flags, int startId) {
//        Log.v(TAG, "AMQP onStartCommand");
////        String msg = null;
////        try {
////            msg = intent.getStringExtra(Constants.AMQP_PUBLISH_MESSAGE);
////        }
////        catch(Exception e){
////            return 0;
////        }
////        if(msg == null)
////            return 0;
////        try {
////            Log.d(TAG,"[q] " + msg);
////            publishQueue.putLast(msg);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//        return 1;
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_FOO.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionFoo(param1, param2);
//            } else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
//        }
//    }
//
//    /**
//     * Handle action Foo in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionFoo(String param1, String param2) {
//        // TODO: Handle action Foo
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//}
