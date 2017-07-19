package recruitment.iiitd.edu.interfaces;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.io.File;

import recruitment.iiitd.edu.utils.Pair;

/**
 * Created by Student on 07-06-2017.
 */

public interface AccelerometerListener extends SensorEventListener {

    void onSensorChanged(SensorEvent var1);

    void onAccuracyChanged(Sensor var1, int var2);

    void start(Context context, int samplingRate, String queryNumber,String ioPair);

    void stop();

}
