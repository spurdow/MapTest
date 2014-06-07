package com.example.maptest;

import org.w3c.dom.Document;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import app.akexorcist.gdaplibrary.GoogleDirection;
import app.akexorcist.gdaplibrary.GoogleDirection.OnDirectionResponseListener;

@SuppressLint("NewApi") public class MainActivity extends SherlockFragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks ,
	GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
	
	private LocationRequest locationRequest;
	
	private LocationClient locationClient;
	
	private Location currentLocation;
	
	private Marker targetMark;
	
	private GoogleDirection googleDirection;
	
	private Document document;
	
	private Polyline target_polyline;
	
	private Polyline current_polyline;
	
	private String mode ;
	
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
	
	private Circle me;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        init();
     
    }

    
    
    @Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
    	this.getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

    

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.action_directions: 
			if( targetMark != null  ){
/*				googleDirection.setLogging(true);
				googleDirection.request(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), targetMark.getPosition(), GoogleDirection.MODE_DRIVING);
				
				*/
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setItems(new CharSequence[]{"Walking","Driving"}, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						googleDirection.setLogging(true);
						switch(which){
						case 0 : mode = GoogleDirection.MODE_WALKING;
						googleDirection.request(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), targetMark.getPosition(), mode);
						break;
						case 1 : mode = GoogleDirection.MODE_DRIVING;
						googleDirection.request(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), targetMark.getPosition(), mode);
						break;
						}
						dialog.dismiss();
					}
					
				});
				builder.create().show();
				
			}else{
				Toast.makeText(this, "Oops!, you should find a target first", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.action_edit: map.clear();
			if(me != null){

				me.setCenter(new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude()));
				me.setFillColor(Color.BLUE);
				me.setRadius(10);
				me.setStrokeColor(Color.WHITE);
				me.setStrokeWidth(5);
			}
			break;
		case R.id.action_settings: Toast.makeText(this, "No Action Created", Toast.LENGTH_LONG).show();break;
		}
		return super.onOptionsItemSelected(item);
		
	}



	@SuppressLint("NewApi") 
    private void init(){
    	if(map == null){
    		map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    		map.setOnMapClickListener(new OnMapClickListener(){

				@Override
				public void onMapClick(LatLng arg0) {
					// TODO Auto-generated method stub
					if(targetMark != null){
						targetMark.remove();
					}
					
					targetMark = map.addMarker(new MarkerOptions()
					.position(arg0));
					//MainActivity.this.getActionBar().show();
				}
    			
    		});
    		if(map == null){
    			Toast.makeText(this, "Cant get Map!", Toast.LENGTH_LONG).show();
    		}
    		
    		if(map != null){
    			/**
    			 * Enable GPS First
    			 */

    			
    			checkGPS();

    		}

    	}
    }
    
    private void checkGPS(){
			if(!isGpsEnabled()){
				promptUser();
			}else{
    			/**
    			 * set location Request and client and google direction
    			 */
				googleDirection = new GoogleDirection(this);
				googleDirection.setOnDirectionResponseListener(new OnDirectionResponseListener(){

					@Override
					public void onResponse(String status, Document doc,
							GoogleDirection gd) {
						// TODO Auto-generated method stub
						if(target_polyline != null){
							target_polyline.remove();
						}
						target_polyline = map.addPolyline(gd.getPolyline(doc, 3, Color.BLUE));
						
						target_polyline.setZIndex(4);
						
						
/*						me = map.addCircle(new CircleOptions()
						.center(new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude()))
						.radius(20)
						.fillColor(Color.BLUE)
						.strokeColor(Color.WHITE)
						.strokeWidth(20));
						
						targetMark = map.addMarker(new MarkerOptions()
						.position(targetMark.getPosition()));*/
					}
					
				});
				locationRequest = LocationRequest.create();
				locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(UPDATE_INTERVAL)
				.setFastestInterval(FASTEST_INTERVAL);

				locationClient = new LocationClient(this , this , this);
				locationClient.connect();
				
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
				startActivityForResult(intent , 1);
			}
    		
    	});

    	
    	AlertDialog dialog = builder.create();
    	dialog.show();
    	
    }
    
    


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		// TODO Auto-generated method stub
		if(requestCode == 1){
			checkGPS();
		}
		
		super.onActivityResult(requestCode, resultCode, arg2);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		init();
		if(locationClient != null){
			locationClient.connect();
		}
		
		
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

		if(currentLocation.getLatitude() != location.getLatitude() &&
				currentLocation.getLongitude() != location.getLongitude()){
			currentLocation = location;
			Toast.makeText(this, "Location Changed!", Toast.LENGTH_LONG).show();
			Log.d("MainActivity", location.getLatitude() + " " + location.getLongitude());
			if(me != null){
				if(current_polyline == null){
					current_polyline = map.addPolyline(new PolylineOptions()
					.add(new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude()))
					.color(Color.RED)
					.width(3)
					.zIndex(5));
				}else{
					current_polyline.getPoints().add(new LatLng(location.getLatitude(), location.getLongitude()));
				}
				me.setCenter(new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude()));
			}
		}
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
            Toast.makeText(this, connectionResult.getErrorCode() + " Connection Failed.", Toast.LENGTH_SHORT).show();
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		currentLocation = locationClient.getLastLocation();

		LatLng coordinates = new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude());
		CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, 16);
		map.animateCamera(camUpdate);
		
		locationClient.requestLocationUpdates(locationRequest, this);
		 me = map.addCircle(new CircleOptions()
		.center(new LatLng(currentLocation.getLatitude() , currentLocation.getLongitude()))
		.radius(10)
		.fillColor(Color.BLUE)
		.strokeColor(Color.WHITE)
		.strokeWidth(5));
		
		
		Log.d("MainActivity", "Connected");
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

		
		super.onStop();
		if(locationClient != null){
			if(locationClient.isConnected()){
				locationClient.removeLocationUpdates(this);
			}
			locationClient.disconnect();
		}
	}
    
    
    
    
}
