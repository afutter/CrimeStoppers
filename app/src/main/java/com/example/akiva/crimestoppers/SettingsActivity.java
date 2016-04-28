package com.example.akiva.crimestoppers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

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
    public static double radius;
    private int progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        radiusBar=(SeekBar)findViewById(R.id.seekBarRadius);

        radiusVal=(TextView)findViewById(R.id.seekbarVal);
        radiusVal.setText(""+radiusBar.getProgress());

        theft=(CheckBox) findViewById(R.id.theftCheckBox);
        robbery=(CheckBox) findViewById(R.id.robberyCheckBox);
        assault=(CheckBox) findViewById(R.id.burglaryCheckBox);
        burglary= (CheckBox) findViewById(R.id.burglaryCheckBox);
        fromautoTheft= (CheckBox) findViewById(R.id.carJackCheckBox);
        sexualAbuse=(CheckBox) findViewById(R.id.sexCheckBox);
        homicide=(CheckBox) findViewById(R.id.homicideCheckBox);
        car=(CheckBox)findViewById(R.id.carCheckBox);
        save=(Button) findViewById(R.id.submitButton);
        reset=(Button)findViewById(R.id.resetButton);


        if(getIntent()!=null){
            Intent intent = getIntent();
            radius = intent.getDoubleExtra("radius", radius);
            radiusVal.setText(radius + "");
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

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusVal.setText(""+seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettings();
                radius=radiusBar.getProgress();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("checkboxes", tracking);
                intent.putExtra("radius", radius);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }
    /*Keys
    * Theft, Robbery,Assault,Burglary,FromAutoTheft,sexualAbuse,Homicide,Car
    * */

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
    public static String checkTracking(String crimeType, double distance){
        if(tracking.containsKey(crimeType)){
            return tracking.get(crimeType)+" "+distance+" miles away!";

        }else {
            return "Not Tracking";
        }
    }

}
