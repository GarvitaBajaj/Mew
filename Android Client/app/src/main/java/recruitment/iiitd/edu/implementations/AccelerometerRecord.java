package recruitment.iiitd.edu.implementations;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import recruitment.iiitd.edu.interfaces.AccelerometerListener;
import recruitment.iiitd.edu.sensing.PublishSensorData;
import recruitment.iiitd.edu.utils.Constants;
import recruitment.iiitd.edu.utils.Pair;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Manan Wason on 07-06-2017.
 */

public class AccelerometerRecord implements AccelerometerListener {
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;
        private ArrayList<String> readings;
        private String queryNo;
        private Context context;
        private String ioPair;

        private SensorManager getInstance(Context context) {
            this.context=context;
            if (mSensorManager == null || mAccelerometer == null) {
                mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            return mSensorManager;
        }

        @Override
        public void start(Context context, int samplingRate, String queryNumber, String ioPair) {
            getInstance(context);
            queryNo = queryNumber;
            this.ioPair=ioPair;
            readings = new ArrayList<>();
            Log.e("TYPE","File name here"+ioPair);
            mSensorManager.registerListener(this, mAccelerometer, samplingRate);
        }

        @Override
        public void stop() {
            mSensorManager.unregisterListener(this);
            writeDataToFile(readings);
        }


        @Override
        public void onSensorChanged(SensorEvent var1) {
            long timeMillis = System.currentTimeMillis();
            Sensor sensor = var1.sensor;
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                readings.add(timeMillis + "," + var1.values[0] + "," + var1.values[1] + "," + var1.values[2]);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor var1, int var2) {

        }

        public void writeDataToFile(ArrayList<String> arrayList) {
            FileOutputStream fileOut = null;
            try {
                File file = new File(Environment.getExternalStorageDirectory() + Constants.FILE_BASE_PATH);
                file.mkdirs();
                File file1 = new File(file, queryNo + "/" + Constants.SENSORS.ACCELEROMETER.getValue() + ".csv");
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
                PublishSensorData.sendAsMessage(context, ioPair , queryNo, "Accelerometer", queryNo.substring(0,16));
           } catch (Exception e1) {
                e1.printStackTrace();
            }

        }
}
