package com.example.akiva.crimestoppers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Parser mParser;
    private Geocoder mGeocoder;
    private Location mLastLocation;
    private double[] mBounds = {38.803149,-77.11974,38.995548,-76.909393};
    //lower left Lat, lower left long, upper right lat, upper right long
    private LocationManager mLocationManager;
    private List<Geofence> mGeofenceList;
    private android.support.design.widget.FloatingActionButton mSettings_button;

    private TreeSet<Crime> mCrimesList;
    private TreeSet<Crime> mBadCrimesList = new TreeSet<Crime>();//for crimes with bad addresses
    private HashMap<String,String> mSettings;
    private double mRadius = 5;


    private final int CIRCLE_SIZE = 1000;
    private final int RED_COLOR = 0x10ff0000;//first 2 digits control the transparency of the color
    private final int START_ZOOM = 13; //change initial zoom with this
    private final String DATA = "ASAP__from03_27_2016__to04_07_2016.xml";
    private final String TAG = "MainActivity";
    public  final static String NOTIFICATION = "notification";
    private final int SETTINGS_REQUEST = 1;
    private final int MAX_CRIMES = 100; //Chance plotted crimes with this
    private LatLng mWashington = new LatLng(38.8951100, -77.0363700);
    private LatLng[] movement= new LatLng[5];



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button demo= (Button) findViewById(R.id.demo);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(null == (mLocationManager =  (LocationManager)getSystemService(Context.LOCATION_SERVICE))){
            finish();
        }

        mSettings_button =(android.support.design.widget.FloatingActionButton)  findViewById(R.id.settingsButton);

        mSettings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                if(mSettings!=null) {
                    intent.putExtra("checkboxes", mSettings);
                }
                intent.putExtra("radius", mRadius);
                startActivityForResult(intent, SETTINGS_REQUEST);
            }
        });
        mGeocoder = new Geocoder(this);
        //This creates an instance of a GoogleAPI client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1) inject crime


            }
        });



    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    //Test Geofence
    public void TestGeofence(){

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mGeocoder = new Geocoder(this, Locale.getDefault());
        // Add a marker in Washington and move the camera

        Marker marker= mMap.addMarker(new MarkerOptions()
                        .position(mWashington)
                        .title("You")
                        .snippet("uh-oh...is crime nearby?")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mWashington));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(START_ZOOM), 2000, null);//2000 animates it for 2 seconds. Zoom lvl 10
        loadCrimes(mWashington);
        //Start Parser;

    }

    private void loadCrimes(LatLng Loc){
        mParser = new Parser();
        try {
            mParser.parse(getResources().getAssets().open(DATA, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCrimesList= mParser.nearestCrimes(Loc.latitude, Loc.longitude, mRadius);
        //Loop through and plot crimes
        List<Address> address;
        int i = 1;
        for(Crime c: mCrimesList){
            try {
                address = mGeocoder.getFromLocationName(c.address ,1,
                        mBounds[0], mBounds[1],mBounds[2], mBounds[3]);
                if(address == null || address.size() == 0){
                    Log.i(TAG, "***Crime:" + c.offense + " @ " + c.address + " NOT PLOTTED");
                    /*Crimes with addresses that can't be found are added to bad crimes list
                    to be removed */
                    mBadCrimesList.add(c);
                    Log.i(TAG, mBadCrimesList.toString());
                    if(i<=MAX_CRIMES) {
                        continue;
                    }else{
                        removeBadCrimes();
                        return;
                    }
                }
                LatLng coor = new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude());
                Marker crime_marker = mMap.addMarker(new MarkerOptions()
                        .position(coor));
                Circle crime_circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(coor.latitude, coor.longitude))
                        .radius(CIRCLE_SIZE)
                        .strokeColor(Color.RED)
                        .fillColor(RED_COLOR));
                c.circle = crime_circle;
                c.marker = crime_marker;
                if(c.marker == null){
                    Log.i(TAG, "weee gotta nulllll marker!!!!");
                }
                if(c.circle == null){
                    Log.i(TAG, "weee gotta nulllll circle!!!!");
                }
                c.Lat = coor.latitude;
                c.Long = coor.longitude;
                c.setVisable(false);
                Log.i(TAG, "Crime:" + c.offense + " @ " + c.address + " PLOTTED");
            }catch(IOException e){
                Log.i(TAG, "I/O EXCEPTION while plotting crime");
                mBadCrimesList.add(c);;
                continue;
            }
            i++;
            if(i > MAX_CRIMES ){
                removeBadCrimes();
                return;
            }
        }

    }
    private void removeBadCrimes(){
        for(Crime badCrime: mBadCrimesList){
            mCrimesList.remove(badCrime);
            Log.i(TAG, "bad crime removed");
        }
    }
    private void addCrime(){


    }
    /*:: Distance                                                                :*/
    /*::  Passed to function:                                                    :*/
    /*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
    /*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
    /*::    unit = the unit you desire for results                               :*/
    /*::           where: 'M' is statute miles (default)                         :*/
    /*::                  'K' is kilometers                                      :*/
    /*::                  'N' is nautical miles                                  :*/
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equals("K")) {
            dist = dist * 1.609344;
        } else if (unit.equals("N")) {
            dist = dist * 0.8684;
        }
        //default miles
        return (dist);
    }
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSettings = (HashMap<String, String>) data.getSerializableExtra("checkboxes");
        mRadius =  data.getDoubleExtra("radius", mRadius);
        Log.i(TAG,"*********Back to MAIN_ACTIVITY*********");
        Log.i(TAG, "SETTINGS DATA:");
        Log.i(TAG, mSettings.toString());
        Log.i(TAG, "Radius: " + mRadius);
        Log.i(TAG, "**************************************");

        //change visability of crime markers and cricles based on settings here
        int i = 0;
        for(Crime crime: mCrimesList){
            i++;
            Log.i(TAG, "crime " + i);
            boolean visable = false;
            Log.i(TAG, mSettings.keySet().size() + "!");
            for(String offense: mSettings.keySet()){
                if(crime.offense.equals(offense)){
                    Log.i(TAG,  crime.offense+ "!=" + offense);
                        visable = true;
                    //if the crime matches one of the offenses no need to match other offenses
                }else{
                    Log.i(TAG,  crime.offense+ "!=" + offense);
                }
            }
            if(visable == false){
                //if the crime hasn't been set to true by here, no need to check radius
                crime.setVisable(false);
                Log.i(TAG, "Set crime [" + crime.offense + "] @ " + crime.address + "to NOT VISABLE");
                if(i<MAX_CRIMES) {
                    continue;
                }else{
                    return;
                }
            }
            //
            double distance = distance(crime.Lat,
                    crime.Long,
                    mWashington.latitude,
                    mWashington.longitude,
                    "M");
            if(distance > mRadius ){
                crime.setVisable(false);
                Log.i(TAG, "crime @ " + crime.address + " is " + distance + " miles way, " +
                        "which is less than " + mRadius + "miles, the Radius.");
                if(i<MAX_CRIMES) {
                    continue;
                }else{
                    return;
                }

            }
            crime.setVisable(true);
            crime.marker.setTitle(mSettings.get(crime.offense));
            crime.marker.setSnippet(crime.address);
            if(i<MAX_CRIMES) {
                continue;
            }else{
                return;
            }
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private void sendPushNotification (String errorMessage){

        //notification builder
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_warning_24dp)
                        .setContentTitle("DANGER!")
                        .setContentText(errorMessage);

        //create actual intent to pass warning over to notificationResultActivity so that way it can be displayed
        Intent resultIntent= new Intent(this, NotificationResultActivity.class);
        resultIntent.putExtra(NOTIFICATION, errorMessage);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

}
