package edu.dartmouth.cs.neurostare;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final PebbleMessanger pmsg = new PebbleMessanger(getApplicationContext());

        Button win =  (Button) findViewById(R.id.winbutton);
        Button lose =  (Button) findViewById(R.id.losebutton);

        win.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pmsg.sendEndGame(true);
            }
        });

        lose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pmsg.sendEndGame(false);
            }
        });

        private static final String FIREBASE_URL = "https://android-drawing.firebaseIO-demo.com";


        private Firebase mFirebaseRef;

        /**
         * Called when the activity is first created.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mFirebaseRef = new Firebase(FIREBASE_URL);
            mDrawingView = new DrawingView(this, mFirebaseRef);
            setContentView(mDrawingView);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
