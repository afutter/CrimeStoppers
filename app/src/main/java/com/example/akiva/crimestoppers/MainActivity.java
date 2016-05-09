package com.example.akiva.crimestoppers;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
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
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Parser mParser;
    private Geocoder mGeocoder;
    private Location mLastLocation;
    private double[] mBounds = {38.803149,-77.11974,38.995548,-76.909393};
    //lower left Lat, lower left long, upper right lat, upper right long

    private List<Geofence> mGeofenceList;
    private FloatingActionButton mSettings_button;

    private TreeSet<Crime> mCrimesList;
    private TreeSet<Crime> mBadCrimesList = new TreeSet<Crime>();//for crimes with bad addresses
    private HashMap<String,String> mSettings;
    private double mRadius = 5;


    private final int CIRCLE_SIZE = 1000;
    private final int RED_COLOR = 0x10ff0000;//first 2 digits control the transparency of the color
    private final int START_ZOOM = 12; //change initial zoom with this
    private final String DATA = "ASAP__from03_27_2016__to04_07_2016.xml";
    private final String TAG = "MainActivity";
    public  final static String NOTIFICATION = "notification";
    private final int SETTINGS_REQUEST = 1;
    private final int MAX_CRIMES = 100; //Chance plotted crimes with this
    private LatLng mWashington = new LatLng(38.8951100, -77.0363700);


    private LatLng[] movement= new LatLng[4];
    private LocationManager mLocationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button demo = (Button) findViewById(R.id.demo);
        //movement[0]=new LatLng(38.931469, -77.051621 ); //start-->zoo
        movement[0]=new LatLng(38.95447, -77.047119); //rock creek
        movement[1]=new LatLng(38.928030, -77.048810); //cross bridge
        movement[2]=new LatLng(38.923718, -77.047093); //walter pierce park
        movement[3]=new LatLng(38.921652, -77.047068); // bird caught your eye

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //if(null == (mLocationManager =  (LocationManager)getSystemService(Context.LOCATION_SERVICE))){
        //  finish();
        //}

        mSettings_button = (FloatingActionButton) findViewById(R.id.settingsButton);

        mSettings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                if (mSettings != null) {
                    intent.putExtra("checkboxes", mSettings);
                }
                intent.putExtra("radius", mRadius);
                startActivityForResult(intent, SETTINGS_REQUEST);
            }
        });
        mGeocoder = new Geocoder(this);
        //This creates an instance of a GoogleAPI client
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1) inject crime

                if (mLocationManager == null) {
                    turnOnLocationManager();
                }

                Crime c = new Crime("HOMICIDE", "1369 Connecticut Ave NW, Washington, DC 20036, USA", "n/a", "n/a", 38.909627, -77.043381);
                mCrimesList.add(c);
                //for (int i = 0; i < movement.length; i++) {

                turnOnLocationManager();
                    updateLocation(movement[0]);
                    Log.i("Loop", "got to next i. i=" + 0);
                    for (Crime crime : mCrimesList) {
                        if (intersects(crime.Lat, crime.Long, movement[0].latitude, movement[0].longitude)) {
                            //call push
                            String tracker = SettingsActivity.checkTracking(crime.offense,
                                    distance(crime.Lat, crime.Long, movement[0].latitude, movement[0].longitude, "M"));
                            if (!tracker.equals("Not Tracking")) {
                                sendPushNotification(tracker);

                            }
                        }
                    }
                Handler handlerOne = new Handler();
                handlerOne.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Pausing for 4 seconds");
                        updateLocation(movement[1]);
                        Log.i("Loop", "got to next i. i=" + 1);
                        for (Crime crime : mCrimesList) {
                            if (intersects(crime.Lat, crime.Long, movement[1].latitude, movement[1].longitude)) {
                                //call push
                                String tracker = SettingsActivity.checkTracking(crime.offense,
                                        distance(crime.Lat, crime.Long, movement[1].latitude, movement[1].longitude, "M"));
                                if (!tracker.equals("Not Tracking")) {
                                    sendPushNotification(tracker);

                                }
                            }
                        }
                        Handler handlerTwo = new Handler();
                        handlerTwo.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Pausing for 4 seconds");
                                updateLocation(movement[2]);
                                Log.i("Loop", "got to next i. i=" + 2);
                                for (Crime crime : mCrimesList) {
                                    if (intersects(crime.Lat, crime.Long, movement[2].latitude, movement[2].longitude)) {
                                        //call push
                                        String tracker = SettingsActivity.checkTracking(crime.offense,
                                                distance(crime.Lat, crime.Long, movement[2].latitude, movement[2].longitude, "M"));
                                        if (!tracker.equals("Not Tracking")) {
                                            sendPushNotification(tracker);

                                        }
                                    }
                                }
                                Handler handlerThree = new Handler();
                                handlerThree.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Pausing for 4 seconds");
                                        updateLocation(movement[3]);
                                        Log.i("Loop", "got to next i. i=" + 3);
                                        for (Crime crime : mCrimesList) {
                                            Log.i("Finally!!", "hit the scene");
                                            if (intersects(crime.Lat, crime.Long, movement[3].latitude, movement[3].longitude)) {
                                                Log.i("Finally!!", "intersects");
                                                //call push
                                                String tracker = SettingsActivity.checkTracking(crime.offense,
                                                        distance(crime.Lat, crime.Long, movement[3].latitude, movement[3].longitude, "M"));
                                                if (!tracker.equals("Not Tracking")) {
                                                    sendPushNotification(tracker);

                                                }
                                            }
                                        }
                                    }
                                }, 4000);
                            }
                        }, 4000);
                    }
                }, 4000);






                //shutdownLocationManager();
            }

            //shutdownLocationManager();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                   0);
        }

    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.akiva.crimestoppers/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.akiva.crimestoppers/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
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
                /*Circle crime_circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(coor.latitude, coor.longitude))
                        .radius(CIRCLE_SIZE)
                        .strokeColor(Color.RED)
                        .fillColor(RED_COLOR));
                c.circle = crime_circle;*/
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
        if(!data.hasExtra("radius") || !data.hasExtra("checkboxes")){
            Log.i(TAG, "no setting changes");
            return;

        }
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

    private void turnOnLocationManager(){
        mLocationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false,
                false, true, true, true, 0, 5);
        mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationSource);
    }
    public void updateLocation(LatLng location){
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        mMap.setMyLocationEnabled(true);
        Location mockLocation= new Location(LocationManager.GPS_PROVIDER);
        mockLocation.setLatitude(location.latitude);
        mockLocation.setLongitude(location.longitude);
        mockLocation.setAltitude(0);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation
                .setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        mockLocation.setAccuracy(5);
        mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
        Log.i("LocationUpdate", "should have been updated");
        //mMap.setLocationSource(new MyLocationSource(mockLocation));


    }
    private void shutdownLocationManager() {
        mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
    }
    public boolean intersects(double Lat1, double Long1, double Lat2, double Long2){
        if(distance(Lat1, Long2, Lat2, Long2, "M") <= mRadius){
            return true;
        }else{
            return true;
        }
    }


}
