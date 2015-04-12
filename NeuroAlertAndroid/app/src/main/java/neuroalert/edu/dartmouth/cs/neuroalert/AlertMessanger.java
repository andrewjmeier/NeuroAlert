package neuroalert.edu.dartmouth.cs.neuroalert;

import android.content.Context;


import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by Andrew on 4/11/15.
 */
public class AlertMessanger {

    Context context;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("78245FE1-3B00-4F4B-9D7E-F179473DE077");
    private PebbleKit.PebbleDataReceiver mReceiver;
    private static final int
            KEY_BUTTON_EVENT = 0,
            BUTTON_EVENT_UP = 1,
            BUTTON_EVENT_DOWN = 2,
            BUTTON_EVENT_SELECT = 3;
    public AlertMessanger(Context c) {

        context = c;
        PebbleKit.startAppOnPebble(context, PEBBLE_APP_UUID);

        mReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                //ACK the message
                PebbleKit.sendAckToPebble(context, transactionId);

                //Check the key exists
                if(data.getUnsignedIntegerAsLong(KEY_BUTTON_EVENT) != null) {
                    int button = data.getUnsignedIntegerAsLong(KEY_BUTTON_EVENT).intValue();

                    switch(button) {
                        case BUTTON_EVENT_UP:
                            //The UP button was pressed
                            break;
                        case BUTTON_EVENT_DOWN:
                            //The DOWN button was pressed
                            break;
                        case BUTTON_EVENT_SELECT:
                            //The SELECT button was pressed
                            break;
                    }
                }
            }

        };

        PebbleKit.registerReceivedDataHandler(context, mReceiver);

    }

    public void unregister() {
        context.unregisterReceiver(mReceiver);
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