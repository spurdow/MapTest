package com.example.maptest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks ,
	GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
	
	private LocationRequest locationRequest;
	
	private LocationClient locationClient;
	
	private Location currentLocation;
	
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	private GoogleMap map;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        init();
        
		


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @SuppressLint("NewApi") 
    private void init(){
    	if(map == null){
    		map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    		if(map == null){
    			Toast.makeText(this, "Cant get Map!", Toast.LENGTH_LONG).show();
    		}
    		
    		if(map != null){
    			/**
    			 * Enable GPS First
    			 */

    			
    			if(!isGpsEnabled()){
    				promptUser();
    			}else{
        			/**
        			 * set location Request and client
        			 */
    				locationRequest = LocationRequest.create();
    				locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    				.setInterval(UPDATE_INTERVAL)
    				.setFastestInterval(FASTEST_INTERVAL);
    				
    				locationClient = new LocationClient(this , this , this);
    				locationClient.connect();

    			}

    		}

    	}
    }
    
    private boolean isGpsEnabled(){
		final LocationManager manager = (LocationManager) this.getApplicationContext().getSystemService(LOCATION_SERVICE);
		
		boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		return gpsEnabled;
    }
    
    private void promptUser(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Enable Gps")
    	.setMessage("To proceed, you must enable GPS")
    	.setCancelable(false)
    	.setNeutralButton("Settings", new OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
    		
    	});

    	
    	AlertDialog dialog = builder.create();
    	dialog.show();
    	
    }
    


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		init();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.d("MainActivity", location.getLatitude() + " " + location.getLongitude());
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Toast.makeText(this, connectionResult.getErrorCode() + " ", Toast.LENGTH_SHORT).show();
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		currentLocation = locationClient.getLastLocation();

		LatLng coordinates = new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude());
		CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, 5);
		map.animateCamera(camUpdate);
		
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		init();
	}
	
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(locationClient != null){
			locationClient.connect();
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if(locationClient.isConnected()){
			locationClient.removeLocationUpdates(this);
		}
		
		super.onStop();
		if(locationClient != null){
			locationClient.disconnect();
		}
	}
    
    
    
    
}
