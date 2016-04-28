package com.example.akiva.crimestoppers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Iterator;
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
    private TreeSet<Crime> mCrimesList;
    private List<Geofence> mGeofenceList;
    private android.support.design.widget.FloatingActionButton mSettings;

    //first 2 digits control the transparency of the color
    private final int RED_COLOR = 0x10ff0000;
    private final int START_ZOOM = 13;
    private final String DATA = "ASAP__from03_27_2016__to04_07_2016.xml";
    private final String TAG = "Crime_Stoppers";



    private static final int SETTINGS = 0;

    private int mRadius = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(null == (mLocationManager =  (LocationManager)getSystemService(Context.LOCATION_SERVICE))){
            finish();
        }

        mSettings =(android.support.design.widget.FloatingActionButton)  findViewById(R.id.settingsButton);

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
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
        LatLng washington = new LatLng(38.8951100, -77.0363700);
        Marker marker= mMap.addMarker(new MarkerOptions()
                        .position(washington)
                        .title("Washington DC")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        );
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(washington));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(START_ZOOM), 2000, null);//2000 animates it for 2 seconds. Zoom lvl 10
        loadCrimes(washington);
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
        int i = 0;
        for(Crime c: mCrimesList){
            try {
                address = mGeocoder.getFromLocationName(c.address ,1,
                        mBounds[0], mBounds[1],mBounds[2], mBounds[3]);
                if(address == null || address.size() == 0){
                    i++;
                    Log.i(TAG, "Crime:" + c.offense + " @ " + c.address + "NOT PLOTTED");
                    continue;
                }
                LatLng coor = new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude());
                mMap.addMarker(new MarkerOptions().position(coor).title(c.offense
                        + " @ " + c.address));
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(coor.latitude, coor.longitude))
                        .radius(1000)
                        .strokeColor(Color.RED)
                        .fillColor(RED_COLOR));
                Log.i(TAG, "Crime:" + c.offense + " @ " + c.address + "PLOTTED");
            }catch(IOException e){
                Log.i(TAG, "I/O EXCEPTION while plotting crime");
            }
            i++;
            if(i == 9){
                return;
            }
        }
    }

    private void addCrime( Crime crime){


    }
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

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
    void onProviderDisabled(String provider){

    }
    void onProviderEnabled(String provider){

    }
    void onStatusChanged(String provider, int status, Bundle extras){

    }
}
