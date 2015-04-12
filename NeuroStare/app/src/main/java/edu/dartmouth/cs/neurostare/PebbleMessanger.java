package edu.dartmouth.cs.neurostare;

import android.content.Context;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by Andrew on 4/12/15.
 */
public class PebbleMessanger {


    Context context;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("587F2F33-9423-48E2-A1DF-8A70E506AE09");
    private PebbleKit.PebbleDataReceiver mReceiver;
    private static final int
            KEY_BUTTON_EVENT = 0,
            KEY_START_GAME = 23,
            KEY_BLINK_DETECTED = 12,
            KEY_TIME_OF_GAME = 46;

    public PebbleMessanger(Context c) {

        context = c;
        PebbleKit.startAppOnPebble(context, PEBBLE_APP_UUID);

        mReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                //ACK the message
                PebbleKit.sendAckToPebble(context, transactionId);

                //Check the key exists
                if (data.getInteger(KEY_START_GAME) != null) {
                    //int button = data.getInteger(KEY_START_GAME).intValue();
                    Toast.makeText(context, "GAME STARTED!!!", Toast.LENGTH_SHORT).show();
                }

                if (data.getInteger(KEY_TIME_OF_GAME) != null) {
                    long time = data.getInteger(KEY_TIME_OF_GAME).intValue();

                    Toast.makeText(context, "Time: " + time, Toast.LENGTH_SHORT).show();
                }
            }

        };

        PebbleKit.registerReceivedDataHandler(context, mReceiver);

    }

    public void unregister() {
        context.unregisterReceiver(mReceiver);
    }

    public boolean sendEndGame(boolean isWin) {

        //Make the watch vibrate
        PebbleDictionary dict = new PebbleDictionary();
        if(!isWin) {
            dict.addInt32(KEY_BLINK_DETECTED, 0);
        }
       else if(isWin){
            dict.addInt32(KEY_BLINK_DETECTED, 1);
        }
        PebbleKit.sendDataToPebble(context, PEBBLE_APP_UUID, dict);

        return true;
    }
}

