package com.example.akiva.crimestoppers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by Akiva on 4/25/16.
 */
public class SettingsActivity extends Activity {
    TextView radiusVal;
    CheckBox theft;
    CheckBox robbery;
    CheckBox assault;
    CheckBox burglary;
    CheckBox fromautoTheft;
    CheckBox sexualAbuse;
    CheckBox homicide;
    CheckBox car;

    final String THEFT = "THEFT/OTHER";
    final String THEFT_AUTO = "THEFT F/AUTO";
    final String ROBBERY = "ROBBERY";
    final String BURGLARY = "BURGLARY";
    final String MOTOR = "MOTOR VEHICLE THEFT";
    final String ASSAULT = "ASSAULT W/DANGEROUS WEAPON";
    final String SEXUAL = "SEXUAL";
    final String HOMICIDE = "HOMICIDE";

    Button save;
    Button reset;
    SeekBar radiusBar;
    public static HashMap<String, String> tracking= new HashMap<String, String>();
    public static int radius;
    private int progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //set up all buttons, checkboxes, and seekbar
        radiusBar=(SeekBar)findViewById(R.id.seekBarRadius);

        radiusVal=(TextView)findViewById(R.id.seekbarVal);
        radiusVal.setText(""+radiusBar.getProgress());

        theft=(CheckBox) findViewById(R.id.theftCheckBox);
        robbery=(CheckBox) findViewById(R.id.robberyCheckBox);
        assault=(CheckBox) findViewById(R.id.assaultCheckBox);
        burglary= (CheckBox) findViewById(R.id.burglaryCheckBox);
        fromautoTheft= (CheckBox) findViewById(R.id.carJackCheckBox);
        sexualAbuse=(CheckBox) findViewById(R.id.sexCheckBox);
        homicide=(CheckBox) findViewById(R.id.homicideCheckBox);
        car=(CheckBox)findViewById(R.id.carCheckBox);
        save=(Button) findViewById(R.id.submitButton);
        reset=(Button)findViewById(R.id.resetButton);

        //get information to format settings properly
        if(getIntent()!=null){
            Intent intent = getIntent();
            radius = (int)intent.getDoubleExtra("radius", 0.00);
            Log.i("Settings", "Radius from main says " + radius);
            radiusBar.setProgress(radius);
            progress=radius;
            radiusVal.setText(""+radiusBar.getProgress());
            if(intent.hasExtra("checkboxes")){
                tracking = (HashMap<String,String>) intent.getSerializableExtra("checkboxes");
                for(String offense: tracking.keySet()) {
                    if (offense.equals(THEFT)) {
                        theft.setChecked(true);
                    } else if (offense.equals(THEFT_AUTO)) {
                        fromautoTheft.setChecked(true);
                    } else if (offense.equals(BURGLARY)) {
                        burglary.setChecked(true);
                    } else if (offense.equals(SEXUAL)) {
                        sexualAbuse.setChecked(true);
                    } else if (offense.equals(MOTOR)) {
                        car.setChecked(true);
                    } else if (offense.equals(HOMICIDE)) {
                        homicide.setChecked(true);
                    } else if (offense.equals(ASSAULT)) {
                        assault.setChecked(true);
                    } else if (offense.equals(ROBBERY)) {
                        robbery.setChecked(true);
                    }
                }
            }
        }

        //listener for seekbar to update radius
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int incomingProgress, boolean fromUser) {
                //update text of seekbar value, update the seekbar progress
                radiusVal.setText(""+seekBar.getProgress());
                radiusBar.setProgress(incomingProgress);
                progress=incomingProgress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //not implemented
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //not implemented
            }
        });

        //button listener for save button. Saves values and passes them back to MainActivity
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettings();
                radius = radiusBar.getProgress();
                Log.i("Settings", radius + " is the new radius");
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("checkboxes", tracking);
                intent.putExtra("radius", radius);
                setResult(RESULT_OK, intent);
                Log.i("Settings", radius + " really is the new radius");
                finish();
            }
        });

        //button listener for reset button. Resets Values of checkboxes and seekbar on settings page
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theft.setChecked(false);
                robbery.setChecked(false);
                assault.setChecked(false);
                burglary.setChecked(false);
                fromautoTheft.setChecked(false);
                sexualAbuse.setChecked(false);
                homicide.setChecked(false);
                car.setChecked(false);
                radiusBar.setProgress(5);
                progress=5;

            }
        });



    }

    //saves instance of checkboxes for when user goes back to home page.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("theft", theft.isChecked());
        savedInstanceState.putBoolean("robbery", robbery.isChecked());
        savedInstanceState.putBoolean("assault", assault.isChecked());
        savedInstanceState.putBoolean("burglary", burglary.isChecked());
        savedInstanceState.putBoolean("fromautoTheft", fromautoTheft.isChecked());
        savedInstanceState.putBoolean("sexualAbuse", sexualAbuse.isChecked());
        savedInstanceState.putBoolean("homicide", homicide.isChecked());
        savedInstanceState.putBoolean("car", car.isChecked());
        savedInstanceState.putInt("radiusBar", radiusBar.getProgress());
        savedInstanceState.putInt("progress", progress);
    }

    //gets the values of the checkboxes that were saved and updates their values
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        theft.setChecked(savedInstanceState.getBoolean("theft"));
        robbery.setChecked(savedInstanceState.getBoolean("robbery"));
        assault.setChecked(savedInstanceState.getBoolean("assault"));
        burglary.setChecked(savedInstanceState.getBoolean("burglary"));
        fromautoTheft.setChecked(savedInstanceState.getBoolean("fromautoTheft"));
        sexualAbuse.setChecked(savedInstanceState.getBoolean("sexualAbuse"));
        homicide.setChecked(savedInstanceState.getBoolean("homicide"));
        car.setChecked(savedInstanceState.getBoolean("car"));
        radiusBar.setProgress(savedInstanceState.getInt("progress"));


    }




    /*hashmap that stores which crimes are being tracked. Mapping is the Crime Type-->Error Message.
      The String of the Crime type is what is defined from our data set. The Error Message will be
      used later to defein a more complete error message that will include the distance to the error.*/
    private void setSettings(){
        if(theft.isChecked()){
            if(!tracking.containsKey(THEFT)) {
                tracking.put(THEFT, "Warning! Theft occurring");
            }
        }else{
            if(tracking.containsKey(THEFT)){
                tracking.remove(THEFT);
            }
        }

        if(robbery.isChecked()){
            if(!tracking.containsKey(ROBBERY)) {
                tracking.put(ROBBERY, "Warning! Robbery occurring");
            }
        }else{
            if(tracking.containsKey(ROBBERY)){
                tracking.remove(ROBBERY);
            }
        }

        if(assault.isChecked()){
            if(!tracking.containsKey(ASSAULT)) {
                tracking.put(ASSAULT, "Warning! Assault occurring");
            }
        }else{
            if(tracking.containsKey(ASSAULT)){
                tracking.remove(ASSAULT);
            }
        }


        if(burglary.isChecked()){
            if(!tracking.containsKey(BURGLARY)) {
                tracking.put(BURGLARY, "Warning! Burglarly occurring");
            }
        }else{
            if(tracking.containsKey(BURGLARY)){
                tracking.remove(BURGLARY);
            }
        }

        if(fromautoTheft.isChecked()){
            if(!tracking.containsKey(THEFT_AUTO)) {
                tracking.put(THEFT_AUTO, "Warning! Theft of Property from Auto occurring");
            }
        }else{
            if(tracking.containsKey(THEFT_AUTO)){
                tracking.remove(THEFT_AUTO);
            }
        }


        if(sexualAbuse.isChecked()){
            if(!tracking.containsKey(SEXUAL)) {
                tracking.put(SEXUAL, "Warning! Sexual Abuse occurring");
            }
        }else{
            if(tracking.containsKey(SEXUAL)){
                tracking.remove(SEXUAL);
            }
        }

        if(homicide.isChecked()){
            if(!tracking.containsKey(HOMICIDE)) {
                tracking.put(HOMICIDE, "Warning! Homicide occurring");
            }
        }else{
            if(tracking.containsKey(HOMICIDE)){
                tracking.remove(HOMICIDE);
            }
        }

        if(car.isChecked()){
            if(!tracking.containsKey(MOTOR)) {
                tracking.put(MOTOR, "Warning! Car theft ocurring");
            }
        }else{
            if(tracking.containsKey(MOTOR)){
                tracking.remove(MOTOR);
            }
        }




    }

    /*Method called within MainActivity. If a crime is within the user's geofence, this method lets
    * the App know if the user should be alerted about the crime. If the user should be alerted,
    * a more complete error message is built and returned to the main activity
    * */
    public static String checkTracking(String crimeType, double distance){
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_EVEN);

        if(tracking.containsKey(crimeType)){
            return tracking.get(crimeType)+" "+df.format(distance)+" miles away!";

        }else {
            return "Not Tracking";
        }
    }

}
