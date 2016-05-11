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
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    private Circle mRadiusCircle;


    private final double METERS_PER_MILE = 1609.34;
    private final int BLUE_COLOR = 0x2599ccff;//first 2 digits control the transparency of the color
    private final int START_ZOOM = 12; //change initial zoom with this
    private final String DATA = "ASAP__from03_27_2016__to04_07_2016.xml";
    private final String TAG = "MainActivity";
    public  final static String NOTIFICATION = "notification";
    private final int SETTINGS_REQUEST = 1;
    private final int MAX_CRIMES = 100; //Change the number of plotted crimes with this


    private  LatLng mCurrentLoc;
    private LatLng[] movement= new LatLng[4];
    private LocationManager mLocationManager;

    private static final double ASSUMED_INIT_LATLNG_DIFF = 1.0;
    private static final float ACCURACY = 0.01f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button demo = (Button) findViewById(R.id.demo);

        //GPS locations for demo
        movement[0]=new LatLng(38.95447, -77.047119); //rock creek
        movement[1]=new LatLng(38.928030, -77.048810); //cross bridge
        movement[2]=new LatLng(38.923718, -77.047093); //walter pierce park
        movement[3]=new LatLng(38.921652, -77.047068); // bird caught your eye

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Settings button to access settingsActivity. Listener setup below
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

        //setup map
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

        //Button for running the demo. All code within this listener runs entire demo process
        demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //turn on mockLocationManager
                if (mLocationManager == null) {
                    Log.i(TAG, "location manager was null");
                    turnOnLocationManager();
                }

                //Create Fake crime
                Crime c = new Crime("HOMICIDE", "1369 Connecticut Ave NW, Washington, DC 20036, USA", "n/a", "n/a", 38.909627, -77.043381);

                //Make sure crime is not made more than once
                if(mCrimesList.contains(c)){
                    c.setVisable(false);
                    mCrimesList.remove(c);
                }

                //add crime to dataStructure that is storing crimes
                mCrimesList.add(c);
                addCrimeMarker(c);
                c.setVisable(true);


                    /*
                    * Move to four points. Show that when reached a point within the geofence,
                    * a push notification will be sent. Here that point is stored in the movement
                    * array at index 2
                    *
                    * */



                    //update location and adjust map
                    updateLocation(movement[0]);
                    updateRadiusCircle();
                    adjustToRadius();


                    //iterate through crimes to see if there is one within the geofence
                    for (Crime crime : mCrimesList) {
                        //if there is an intersection, see if that crime is being tracked
                        if (intersects(crime.Lat, crime.Long, movement[0].latitude, movement[0].longitude)) {

                            String tracker = SettingsActivity.checkTracking(crime.offense,
                                    distance(crime.Lat, crime.Long, movement[0].latitude, movement[0].longitude, "M"));

                            //if crime type is being tracked, send push notification
                            if (!tracker.equals("Not Tracking")) {
                                sendPushNotification(tracker);

                            }
                        }
                    }

                /*Repeat process for several points. Handler used to create delay and to allow for UI
                * to easily show update in points/location
                * */
                Handler handlerOne = new Handler();
                handlerOne.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Pausing for 4 seconds");
                        updateLocation(movement[1]);
                        updateRadiusCircle();
                        adjustToRadius();

                        for (Crime crime : mCrimesList) {
                            if (intersects(crime.Lat, crime.Long, movement[1].latitude, movement[1].longitude)) {

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
                                updateRadiusCircle();
                                adjustToRadius();

                                for (Crime crime : mCrimesList) {
                                    if (intersects(crime.Lat, crime.Long, movement[2].latitude, movement[2].longitude)) {
                                        //call push
                                        String tracker = SettingsActivity.checkTracking(crime.offense,
                                                distance(crime.Lat, crime.Long, movement[2].latitude, movement[2].longitude, "M"));
                                        if (!tracker.equals("Not Tracking")) {
                                            sendPushNotification(tracker);
                                            Toast.makeText(getApplicationContext(), tracker, Toast.LENGTH_LONG).show();

                                        }
                                    }
                                }
                                Handler handlerThree = new Handler();
                                handlerThree.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Pausing for 4 seconds");
                                        updateLocation(movement[3]);
                                        updateRadiusCircle();
                                        adjustToRadius();

                                        for (Crime crime : mCrimesList) {

                                            if (intersects(crime.Lat, crime.Long, movement[3].latitude, movement[3].longitude)) {

                                                //call push
                                                String tracker = SettingsActivity.checkTracking(crime.offense,
                                                        distance(crime.Lat, crime.Long, movement[3].latitude, movement[3].longitude, "M"));
                                                if (!tracker.equals("Not Tracking")) {
                                                    //sendPushNotification(tracker);

                                                }
                                            }
                                        }
                                    }
                                }, 4000);
                            }
                        }, 4000);
                    }
                }, 4000);







            }

            //shutdownLocationManager();
        });

        //check permission to make sure that user has allowed permission to Use app within the app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }

    }

    protected void onStart() {

        //start up map
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

        //shut down the map
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

        updateScreenLoc(movement[0]);

        loadCrimes(movement[0]);
        //Start MockLocationManager and inject first point
        turnOnLocationManager();
        updateLocation(movement[0]);
        updateRadiusCircle();


    }

    //update location of circle based on current user location
    private void updateRadiusCircle(){
        if(mRadiusCircle != null) {
            mRadiusCircle.setVisible(false);
        }
        mRadiusCircle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mCurrentLoc.latitude, mCurrentLoc.longitude))
                .radius(mRadius * METERS_PER_MILE)
                .strokeColor(Color.BLUE)
                .fillColor(BLUE_COLOR));
        mRadiusCircle.setVisible(true);
    }

    //moves the map to center around current user location
    private void updateScreenLoc(LatLng loc){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(START_ZOOM), 2000, null);//2000 animates it for 2 seconds. Zoom lvl 10
    }

    //adjusts size of map based on radius size
    private void adjustToRadius(){
        float distance = (float)(2 *METERS_PER_MILE * mRadius);
        LatLngBounds bounds = boundsWithCenterAndLatLngDistance(
                new LatLng(mCurrentLoc.latitude, mCurrentLoc.longitude)
                , distance, distance);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0), 1000, null);
    }

    //load crimes from external data source
    private void loadCrimes(LatLng Loc){
        //create a new instance of parser class. Parser class queries Data.octo.dc.gov for crime data
        mParser = new Parser();

        //try and parse data
        try {
            mParser.parse(getResources().getAssets().open(DATA, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //get the data from the parser and stores in TreeSet
        mCrimesList= mParser.nearestCrimes(Loc.latitude, Loc.longitude, mRadius);

        //Loop through and plot crimes
        List<Address> address;
        int i = 1;

        //iterate through crimes and querey google to get latitude and longitude of particular addresses
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

                //Create pin
                Marker crime_marker = mMap.addMarker(new MarkerOptions()
                        .position(coor));

                //this put geofence around crime points. Team decided to comment out to remove clutter
                /*Circle crime_circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(coor.latitude, coor.longitude))
                        .radius(CIRCLE_SIZE)
                        .strokeColor(Color.RED)
                        .fillColor(RED_COLOR));
                c.circle = crime_circle;*/


                //crime object gets reference to its own pin
                c.marker = crime_marker;
                if(c.marker == null){
                    Log.i(TAG, "null marker");
                }
                if(c.circle == null){
                    Log.i(TAG, "null circle");
                }
                c.Lat = coor.latitude;
                c.Long = coor.longitude;

                //start out crime by being invisible
                c.setVisable(false);
                Log.i(TAG, "Crime:" + c.offense + " @ " + c.address + " PLOTTED");
            }catch(IOException e){
                //catch any issue that google throws
                Log.i(TAG, "I/O EXCEPTION while plotting crime");
                mBadCrimesList.add(c);
                continue;
            }
            i++;
            if(i > MAX_CRIMES ){
                removeBadCrimes();
                return;
            }
        }

    }
    //remove bad crimes that google had an issue with (in terms of formatting)
    private void removeBadCrimes(){
        for(Crime badCrime: mBadCrimesList){
            mCrimesList.remove(badCrime);
            Log.i(TAG, "bad crime removed");
        }
    }

    //add pin to map
    private void addCrimeMarker(Crime c){
        LatLng coor = new LatLng(c.Lat, c.Long);
            Marker crime_marker = mMap.addMarker(new MarkerOptions()
                    .position(coor));
            c.marker = crime_marker;
            if(c.marker == null){
                Log.i(TAG, "weee gotta nulllll marker!!!!");
            }
            c.setVisable(true);
            Log.i(TAG, "MockCrime:" + c.offense + " @ " + c.address + " PLOTTED");
    }
    /*:: CalculateDistance                                                       :*/
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

    //get info back from settings page
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != SETTINGS_REQUEST){
            Log.i(TAG, "onActivityResult():incorrect Request Code!");
            return;
        }
        if(resultCode == RESULT_CANCELED){
            Log.i(TAG, "onActivityResult(): user backed out of settings page!");
            return;
        }
        if(!data.hasExtra("radius") || !data.hasExtra("checkboxes")){
            Log.i(TAG, "no setting changes");
            return;

        }
        //get info on which crimes are being tracked and the radius being used
        mSettings = (HashMap<String, String>) data.getSerializableExtra("checkboxes");
        mRadius = data.getIntExtra("radius", -1);
        updateRadiusCircle();
        adjustToRadius();


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
                    Log.i(TAG,  crime.offense+ "=" + offense + ", " + crime.address);
                        visable = true;
                    //if the crime matches one of the offenses no need to match other offenses
                }else{
                    Log.i(TAG,  crime.offense+ "!=" + offense + ", " + crime.address);
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
                    mCurrentLoc.latitude,
                    mCurrentLoc.longitude,
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
        //not implemented
    }

    @Override
    public void onConnectionSuspended(int i) {
        //not implemented
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //not implemented
    }

    @Override
    public void onLocationChanged(Location location) {
        //not implemented
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

        //build pending intent to pass to notificaiton bar
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //pass pending intent to notificaiton builder
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        //id of notification
        int mNotificationId = 001;

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    //turn on functionality to allow for mock location injection
    private void turnOnLocationManager(){
        mLocationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false,
                false, true, true, true, 0, 5);
        mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationSource);
    }
    public void updateLocation(LatLng location){

        //check permissions that its ok for user to use maps in app
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        //allow map to display mockLocations
        mMap.setMyLocationEnabled(true);

        //setup Location manager
        Location mockLocation= new Location(LocationManager.GPS_PROVIDER);

        //build mock location using LatLng passed in
        mockLocation.setLatitude(location.latitude);
        mockLocation.setLongitude(location.longitude);
        mockLocation.setAltitude(0);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        mockLocation.setAccuracy(5);

        //add mockLoction to the manager
        mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
        mCurrentLoc = location;
        Log.i("LocationUpdate", "should have been updated");



    }

    //check to see if point is within geofense
    public boolean intersects(double Lat1, double Long1, double Lat2, double Long2){
        if(distance(Lat1, Long1, Lat2, Long2, "M") <= mRadius){
            return true;
        }else{
            return false;
        }
    }



    //set the screen size to be sized around the radius.
    //Source: https://stackoverflow.com/questions/6224671/mkcoordinateregionmakewithdistance-equivalent-in-android
    public static LatLngBounds boundsWithCenterAndLatLngDistance(LatLng center, float latDistanceInMeters, float lngDistanceInMeters) {
        latDistanceInMeters /= 2;
        lngDistanceInMeters /= 2;
        LatLngBounds.Builder builder = LatLngBounds.builder();
        float[] distance = new float[1];
        {
            boolean foundMax = false;
            double foundMinLngDiff = 0;
            double assumedLngDiff = ASSUMED_INIT_LATLNG_DIFF;
            do {
                Location.distanceBetween(center.latitude, center.longitude, center.latitude, center.longitude + assumedLngDiff, distance);
                float distanceDiff = distance[0] - lngDistanceInMeters;
                if (distanceDiff < 0) {
                    if (!foundMax) {
                        foundMinLngDiff = assumedLngDiff;
                        assumedLngDiff *= 2;
                    } else {
                        double tmp = assumedLngDiff;
                        assumedLngDiff += (assumedLngDiff - foundMinLngDiff) / 2;
                        foundMinLngDiff = tmp;
                    }
                } else {
                    assumedLngDiff -= (assumedLngDiff - foundMinLngDiff) / 2;
                    foundMax = true;
                }
            } while (Math.abs(distance[0] - lngDistanceInMeters) > lngDistanceInMeters * ACCURACY);
            LatLng east = new LatLng(center.latitude, center.longitude + assumedLngDiff);
            builder.include(east);
            LatLng west = new LatLng(center.latitude, center.longitude - assumedLngDiff);
            builder.include(west);
        }
        {
            boolean foundMax = false;
            double foundMinLatDiff = 0;
            double assumedLatDiffNorth = ASSUMED_INIT_LATLNG_DIFF;
            do {
                Location.distanceBetween(center.latitude, center.longitude, center.latitude + assumedLatDiffNorth, center.longitude, distance);
                float distanceDiff = distance[0] - latDistanceInMeters;
                if (distanceDiff < 0) {
                    if (!foundMax) {
                        foundMinLatDiff = assumedLatDiffNorth;
                        assumedLatDiffNorth *= 2;
                    } else {
                        double tmp = assumedLatDiffNorth;
                        assumedLatDiffNorth += (assumedLatDiffNorth - foundMinLatDiff) / 2;
                        foundMinLatDiff = tmp;
                    }
                } else {
                    assumedLatDiffNorth -= (assumedLatDiffNorth - foundMinLatDiff) / 2;
                    foundMax = true;
                }
            } while (Math.abs(distance[0] - latDistanceInMeters) > latDistanceInMeters * ACCURACY);
            LatLng north = new LatLng(center.latitude + assumedLatDiffNorth, center.longitude);
            builder.include(north);
        }
        {
            boolean foundMax = false;
            double foundMinLatDiff = 0;
            double assumedLatDiffSouth = ASSUMED_INIT_LATLNG_DIFF;
            do {
                Location.distanceBetween(center.latitude, center.longitude, center.latitude - assumedLatDiffSouth, center.longitude, distance);
                float distanceDiff = distance[0] - latDistanceInMeters;
                if (distanceDiff < 0) {
                    if (!foundMax) {
                        foundMinLatDiff = assumedLatDiffSouth;
                        assumedLatDiffSouth *= 2;
                    } else {
                        double tmp = assumedLatDiffSouth;
                        assumedLatDiffSouth += (assumedLatDiffSouth - foundMinLatDiff) / 2;
                        foundMinLatDiff = tmp;
                    }
                } else {
                    assumedLatDiffSouth -= (assumedLatDiffSouth - foundMinLatDiff) / 2;
                    foundMax = true;
                }
            } while (Math.abs(distance[0] - latDistanceInMeters) > latDistanceInMeters * ACCURACY);
            LatLng south = new LatLng(center.latitude - assumedLatDiffSouth, center.longitude);
            builder.include(south);
        }
        return builder.build();
    }

}
