package recruitment.iiitd.edu.implementations;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import recruitment.iiitd.edu.interfaces.MicListener;
import recruitment.iiitd.edu.sensing.PublishSensorData;
import recruitment.iiitd.edu.utils.Constants;


/**
 * Created by Manan Wason on 30/11/16.
 */

public class MicRecord implements MicListener {
    private static int POLL_INTERVAL = 1000;
    private static final String TAG = "MicRecord";
    static final private double EMA_FILTER = 0.6;
    private String ioPair;
    private Context context;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    File directory;
    File file;
    BufferedWriter writer;

    private boolean mRunning = false;

    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();

    private ArrayList<String> recordedValues;
    private Runnable mSleepTask;
    public static int i = 0;
    private String queryNo = "";


    public void startRecording(final Context context, final int sampleRate, final String queryNo,final String file) {
        this.context=context;
        this.ioPair=file;
        this.queryNo = queryNo;
        POLL_INTERVAL = sampleRate;
        recordedValues = new ArrayList<>();
        startService(context);
        if (checkMode(context) == 0) {
            start(context, sampleRate, queryNo,ioPair);
        }

    }


    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = getAmplitude();
            long time = System.currentTimeMillis();
            recordedValues.add(time + ", " + amp + ", " + getSurface() + ", " + getAudioSourceMax() + ", " + getMaxAmplitude() + ", " +
                    getAmplitude() + ", " + getAmplitudeEMA());
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

    public void startService(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");

    }

    @Override
    public void stop() {
        writeDataToFile(recordedValues);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        mRunning = false;
    }

    @Override
    public int getMaxAmplitude() {
        return mRecorder.getMaxAmplitude();
    }

    @Override
    public Surface getSurface() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return null;
        } else {
            return null;
        }
    }

    @Override
    public int getAudioSourceMax() {
        return 0;
    }


    @Override
    public void start(Context context, int sampleRate, String queryNumber,String ioPair) {
        Log.e("MIC RECORDING", " STARTED ");
        queryNo = queryNumber;
        this.ioPair=ioPair;
        if (mRecorder == null) {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                //  Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                //  Auto-generated catch block
                e.printStackTrace();
            }

            mRecorder.start();
            mEMA = 0.0;
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }



    public double getAmplitude() {
        if (mRecorder != null) {
            return 20 * Math.log10(mRecorder.getMaxAmplitude() / 2700.0);
        } else {
            return 0;
        }

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }


    public void writeDataToFile(ArrayList<String> arrayList) {
        FileOutputStream fileOut = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + Constants.FILE_BASE_PATH);
            file.mkdirs();
            File file1 = new File(file, queryNo + "/" + Constants.SENSORS.MIC.getValue() + ".csv");
            fileOut = new FileOutputStream(file1);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fileOut);
            for (String string : arrayList) {
                myOutWriter.append(string + "\n");
            }
            myOutWriter.close();

            fileOut.flush();
            fileOut.close();
            fileOut=null;
            System.gc();
            PublishSensorData.sendAsMessage(context, ioPair , queryNo, "Microphone", queryNo.substring(0,16));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    public int checkMode(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.getMode();
    }

}
