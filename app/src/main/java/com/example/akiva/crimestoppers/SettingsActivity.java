package com.example.akiva.crimestoppers;

import android.app.Activity;
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

            }
        });


    }
    /*Keys
    * Theft, Robbery,Assault,Burglary,FromAutoTheft,sexualAbuse,Homicide,Car
    * */

    private void setSettings(){
        if(theft.isChecked()){
            if(!tracking.containsKey("Theft")) {
                tracking.put("Theft", "Warning! Theft occurring");
            }
        }else{
            if(tracking.containsKey("Theft")){
                tracking.remove("Theft");
            }
        }

        if(robbery.isChecked()){
            if(!tracking.containsKey("Robbery")) {
                tracking.put("Robbery", "Warning! Robbery occurring");
            }
        }else{
            if(tracking.containsKey("Robbery")){
                tracking.remove("Robbery");
            }
        }

        if(assault.isChecked()){
            if(!tracking.containsKey("Assault")) {
                tracking.put("Assault", "Warning! Assault occurring");
            }
        }else{
            if(tracking.containsKey("Assault")){
                tracking.remove("Assault");
            }
        }


        if(burglary.isChecked()){
            if(!tracking.containsKey("Burglary")) {
                tracking.put("Burglary", "Warning! Burglarly occurring");
            }
        }else{
            if(tracking.containsKey("Burglary")){
                tracking.remove("Burglary");
            }
        }

        if(fromautoTheft.isChecked()){
            if(!tracking.containsKey("FromAutoTheft")) {
                tracking.put("FromAutoTheft", "Warning! Theft of Property from Auto occurring");
            }
        }else{
            if(tracking.containsKey("FromAutoTheft")){
                tracking.remove("FromAutoTheft");
            }
        }


        if(sexualAbuse.isChecked()){
            if(!tracking.containsKey("sexualAbuse")) {
                tracking.put("sexualAbuse", "Warning! Sexual Abuse occurring");
            }
        }else{
            if(tracking.containsKey("sexualAbuse")){
                tracking.remove("sexualAbuse");
            }
        }

        if(homicide.isChecked()){
            if(!tracking.containsKey("Homicide")) {
                tracking.put("Homicide", "Warning! Homicide occurring");
            }
        }else{
            if(tracking.containsKey("Homicide")){
                tracking.remove("Homicide");
            }
        }

        if(car.isChecked()){
            if(!tracking.containsKey("Car")) {
                tracking.put("Car", "Warning! Car theft ocurring");
            }
        }else{
            if(tracking.containsKey("Car")){
                tracking.remove("Car");
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
