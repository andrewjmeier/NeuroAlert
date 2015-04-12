package edu.dartmouth.cs.neurostare;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Andrew on 4/12/15.
 */
public class PebbleMessanger {


    protected Context context = null;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("587F2F33-9423-48E2-A1DF-8A70E506AE09");
    private PebbleKit.PebbleDataReceiver mReceiver;
    private static final int
            KEY_BUTTON_EVENT = 0,
            KEY_START_GAME = 23,
            KEY_BLINK_DETECTED = 12,
            KEY_TIME_OF_GAME = 46;

    protected String game = null, player = null;
    private static final String FIREBASE_URL = "https://neuroalert.firebaseio.com";
    protected Firebase mFirebaseRef = null;
    protected ChildEventListener gameUpdates = null;
    protected Timer timer = null;


    public PebbleMessanger(Context c) {

        context = c;
        game = ((MainActivity) c).game;
        player = ((MainActivity) c).player;
        PebbleKit.startAppOnPebble(context, PEBBLE_APP_UUID);
        mFirebaseRef = new Firebase(FIREBASE_URL);



        mReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveData(final Context context, int transactionId, PebbleDictionary data) {
                //ACK the message
                PebbleKit.sendAckToPebble(context, transactionId);

                //Check the key exists
                if (data.getInteger(KEY_START_GAME) != null) {
                    //int button = data.getInteger(KEY_START_GAME).intValue();
                    Toast.makeText(context, "GAME STARTED!!!", Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).isGameStarted = true;
                    timer = new Timer();

                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            ((MainActivity) context).dataListener.updateTimeSinceBlink();
                        }
                    }, 0, 1000);//Update text every second

                }

                if (data.getInteger(KEY_TIME_OF_GAME) != null) {
                    long time = data.getInteger(KEY_TIME_OF_GAME).intValue();

                    Toast.makeText(context, "Time: " + time, Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).isGameStarted = false;

                }
            }

        };

        PebbleKit.registerReceivedDataHandler(context, mReceiver);

    }

    public void setGameData(String gameParam, String playerParam){
        game = gameParam;
        player = playerParam;

        gameUpdates = mFirebaseRef.getRoot().child("games").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Object snapObj = dataSnapshot.getValue();
                Map<String, Object> obj = (HashMap<String, Object>) snapObj;
                Log.i("PebbleMessanger", "added: " + obj.toString());
                if(obj.get("loser") != null){
                    if(obj.get("loser").equals(player)){
                        sendEndGame(true);
                    } else {
                        sendEndGame(false);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Object snapObj = dataSnapshot.getValue();
                Map<String, Object> obj = (HashMap<String, Object>) snapObj;
                Log.i("PebbleMessanger", "changed: " + obj.toString());
                if(obj.get("loser") != null){
                    if(obj.get("loser").equals(player)){
                        sendEndGame(true);
                    } else {
                        sendEndGame(false);
                    }
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
//        {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                Map<String, Object> map = (HashMap<String, Object>) mFirebaseRef.getRoot().child("games").child(game);
//                Iterable<DataSnapshot> obj = dataSnapshot.getChildren();
//                Log.i("PebbleMessanger", obj.toString());
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });


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

