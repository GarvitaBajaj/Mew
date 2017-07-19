package recruitment.iiitd.edu.interfaces;

import android.content.Context;
import android.view.Surface;

/**
 * Created by Manan Wason on 30/11/16.
 */

public interface MicListener {

    void stop();

    int getMaxAmplitude();

    Surface getSurface();

    int getAudioSourceMax();

    void start(Context context, int sampleRate, String queryNumber,String file);

}
