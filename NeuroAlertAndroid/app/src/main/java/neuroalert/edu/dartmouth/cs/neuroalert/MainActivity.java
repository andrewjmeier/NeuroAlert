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

public class MainActivity extends Activity {

    private TextView mButtonView;
    private Button aButton;
    private AlertMessanger alrtMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonView = new TextView(this);
        mButtonView.setText("No button yet!");

        alrtMsg = new AlertMessanger(getApplicationContext());

        aButton = (Button) findViewById(R.id.button);
        aButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alrtMsg.sendAlert(1);
            }
        });
        //setContentView(mButtonView);
    }

    @Override
    protected void onDestroy() {
        alrtMsg.unregister();
        super.onDestroy();

    }
}