package com.ra1ph.getpic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

	Context context;
	// flag for GPS status
    boolean isGPSEnabled = false;
 
    // flag for network status
    boolean isNetworkEnabled = false;
 
    // flag for GPS status
    boolean canGetLocation = false;
 
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
 
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute
 
    // Declaring a Location Manager
    protected LocationManager locationManager;
	
	public GPSTracker(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		getLocation();
	}
	
	 public Location getLocation() {
	        try {
	            locationManager = (LocationManager) context
	                    .getSystemService(LOCATION_SERVICE);
	 
	            // getting GPS status
	            isGPSEnabled = locationManager
	                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
	 
	            // getting network status
	            isNetworkEnabled = locationManager
	                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	 
	            if (!isGPSEnabled && !isNetworkEnabled) {
	                // no network provider is enabled
	            } else {
	                this.canGetLocation = true;
	                // if GPS Enabled get lat/long using GPS Services
	                if (isGPSEnabled) {

	                        locationManager.requestLocationUpdates(
	                                LocationManager.GPS_PROVIDER,
	                                MIN_TIME_BW_UPDATES,
	                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                        Log.d("GPS Enabled", "GPS Enabled");
	                        if (locationManager != null) {
	                            location = locationManager
	                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                            if (location != null) {
	                                latitude = location.getLatitude();
	                                longitude = location.getLongitude();
	                            }
	                        }

	                }
                    // First get location from Network Provider
                    if (isNetworkEnabled) {
                        if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                        }
                    }
	            }
                Log.d("myLog", "Location is "+Double.toString(location.getLatitude())+"     "+Double.toString(location.getLongitude()));
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        

	 
	        return location;
	    }
	 
	 public void stopUsingGPS(){
	        if(locationManager != null){
	            locationManager.removeUpdates(GPSTracker.this);
	        }       
	    }
	
	 
	 public double getLatitude(){
	        if(location != null){
	            latitude = location.getLatitude();
	        }
	         
	        // return latitude
	        return latitude;
	    }
	     
	    /**
	     * Function to get longitude
	     * */
	    public double getLongitude(){
	        if(location != null){
	            longitude = location.getLongitude();
	        }
	         
	        // return longitude
	        return longitude;
	    }
	     
	    /**
	     * Function to check GPS/wifi enabled
	     * @return boolean
	     * */
	    public boolean canGetLocation() {
	        return this.canGetLocation;
	    }
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
