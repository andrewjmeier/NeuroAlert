package neuroalert.edu.dartmouth.cs.neuroalert;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener {

    boolean connected = false;

    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;
            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView statusText =
                                (TextView) findViewById(R.id.con_status);
                        statusText.setText(status);
                        TextView museVersionText =
                                (TextView) findViewById(R.id.version);
                        if (current == ConnectionState.CONNECTED) {
                            MuseVersion museVersion = muse.getMuseVersion();
                            String version = museVersion.getFirmwareType() +
                                    " - " + museVersion.getFirmwareVersion() +
                                    " - " + Integer.toString(
                                    museVersion.getProtocolVersion());
                            museVersionText.setText(version);
                            connected = true;
                        } else {
                            museVersionText.setText(R.string.undefined);
                            connected = false;
                        }
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;
        ArrayList<Double> alphas = new ArrayList<>();
        ArrayList<Double> betas = new ArrayList<>();
        ArrayList<Double> deltas = new ArrayList<>();
        ArrayList<Double> thetas = new ArrayList<>();
        ArrayList<Double> gammas = new ArrayList<>();
        long lastBlinkTime = -1;
        long durationSinceBlink = 0;
        double blinkRate = 0;
        int maxSize = 4000;


        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    updateTimeSinceBlink();
                }
            }, 0, 1000);//Update text every second

        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
                case ACCELEROMETER:
                    break;
                default:
                    break;
            }


        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if(p.getHeadbandOn() ) {
                if (p.getBlink()) {
                    Log.i("Artifacts", "blink");
                    durationSinceBlink = secondsSinceLastBlink(System.currentTimeMillis(), true);
                } else if (p.getJawClench()) {
                    Log.i("Artifacts", "jaw clench");
                }
            }
        }

        public long secondsSinceLastBlink(long blinkTime, boolean isBlinking){
            if(lastBlinkTime != -1){
                long seconds = (blinkTime - lastBlinkTime) / 1000;
                if(isBlinking) {
                    lastBlinkTime = blinkTime;
                }
                return seconds;
            } else {
                lastBlinkTime = blinkTime;
                return 0;
            }
        }
        public double calcAverage(ArrayList<Double> nums){
            double total = 0;
            for(double n : nums){
                total += n;
            }
            return total / nums.size();
        }

        private void updateTimeSinceBlink(){
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView blinks = (TextView) findViewById(R.id.secondsSinceBlink);
                        if(connected) {
                            durationSinceBlink = secondsSinceLastBlink(System.currentTimeMillis(), false);
                            blinks.setText("" + durationSinceBlink);
                        } else {
                            blinks.setText("0");
                        }
                        if(durationSinceBlink > 10 && (durationSinceBlink - 1) % 5 == 0){
                            alrtMsg.sendAlert(1);
                        }
                    }
                });
            }


        }


        public double getCurrentAverage(double d1, double d2, double d3, double d4){
            double[] values = new double[]{ d1, d2, d3, d4};
            int valid = 0;
            double average = 0;
            for(int i = 0; i < values.length; i++){
                if(!Double.isNaN(values[i])){
                    average += values[i];
                    valid++;
                }
            }

            average = average / valid;
            if(Double.isNaN(average)){
                return 0;
            }
            return average;

        }

    }


    private TextView mButtonView;
    private Button aButton;
    private AlertMessanger alrtMsg;

    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;

    public MainActivity() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity =
                new WeakReference<Activity>(this);
        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonView = new TextView(this);
        mButtonView.setText("No button yet!");
        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);
        Button pauseButton = (Button) findViewById(R.id.pause);
        pauseButton.setOnClickListener(this);

        alrtMsg = new AlertMessanger(getApplicationContext());
//
//        alrtMsg.sendAlert(1);
//        aButton = (Button) findViewById(R.id.button);
//        aButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        //setContentView(mButtonView);
    }

    @Override
    public void onClick(View v) {
        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_spinner);
        if (v.getId() == R.id.refresh) {
            MuseManager.refreshPairedMuses();
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            List<String> spinnerItems = new ArrayList<String>();
            for (Muse m: pairedMuses) {
                String dev_id = m.getName() + "-" + m.getMacAddress();
                Log.i("Muse Headband", dev_id);
                spinnerItems.add(dev_id);
            }
            ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
                    this, android.R.layout.simple_spinner_item, spinnerItems);
            musesSpinner.setAdapter(adapterArray);
        }
        else if (v.getId() == R.id.connect) {
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            if (pairedMuses.size() < 1 ||
                    musesSpinner.getAdapter().getCount() < 1) {
                Log.w("Muse Headband", "There is nothing to connect to");
            }
            else {
                muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
                ConnectionState state = muse.getConnectionState();
                if (state == ConnectionState.CONNECTED ||
                        state == ConnectionState.CONNECTING) {
                    Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
                    return;
                }
                configure_library();
                /**
                 * In most cases libmuse native library takes care about
                 * exceptions and recovery mechanism, but native code still
                 * may throw in some unexpected situations (like bad bluetooth
                 * connection). Print all exceptions here.
                 */
                try {
                    muse.runAsynchronously();
                } catch (Exception e) {
                    Log.e("Muse Headband", e.toString());
                }
            }
        }
        else if (v.getId() == R.id.disconnect) {
            if (muse != null) {
                /**
                 * true flag will force libmuse to unregister all listeners,
                 * BUT AFTER disconnecting and sending disconnection event.
                 * If you don't want to receive disconnection event (for ex.
                 * you call disconnect when application is closed), then
                 * unregister listeners first and then call disconnect:
                 * muse.unregisterAllListeners();
                 * muse.disconnect(false);
                 */
                muse.disconnect(true);
            }
        }
        else if (v.getId() == R.id.pause) {
            dataTransmission = !dataTransmission;
            if (muse != null) {
                muse.enableDataTransmission(dataTransmission);
            }
        }
    }

    private void configure_library() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ACCELEROMETER);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ARTIFACTS);



        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }
    protected void onDestroy() {
        alrtMsg.unregister();
        super.onDestroy();

    }
}