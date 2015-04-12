package neuroalert.edu.dartmouth.cs.neuroalert;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by Andrew on 4/11/15.
 */
public class AlertMessanger {

    Context context;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("78245FE1-3B00-4F4B-9D7E-F179473DE077");

    public AlertMessanger(Context c) {

        context = c;
        PebbleKit.startAppOnPebble(context, PEBBLE_APP_UUID);
    }

    public boolean sendAlert(int msgChoice) {

        //Make the watch vibrate
        PebbleDictionary dict = new PebbleDictionary();
//                dict.addInt32(4, 69);
        dict.addString(1, "HEY MICKEY! How are you doing during this hackathon? I'm getting sleepy h");
        PebbleKit.sendDataToPebble(context, PEBBLE_APP_UUID, dict);

        return true;
    }

}