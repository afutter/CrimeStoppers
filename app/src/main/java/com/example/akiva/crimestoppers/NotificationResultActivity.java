package com.example.akiva.crimestoppers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Akiva on 5/6/16.
 */
public class NotificationResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setup view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_result);

        TextView warning= (TextView) findViewById(R.id.info);
        Button toMap= (Button) findViewById(R.id.toMap);

        //get error message from intent
        if(getIntent()!=null){
            warning.append(" "+getIntent().getStringExtra(MainActivity.NOTIFICATION));
        }

        //button listener to return to app main page with map
        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


}
