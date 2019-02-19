package edu.usf.andres.locationandmaps;


import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    static final LatLng usf = new LatLng(28.061816, -82.411282);

    private static final String TAG = "Debug";
    private static final String fLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String cLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int ERROR_CODE_REQUEST = 9999;
    private static final int LOCATION_REQUEST_CODE = 1111;

    private Boolean mLocationGranted = false;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private Marker lastLocationMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (!CheckServices()) {
            Log.d(TAG, "BAD SERVICES");
            return;
        }
        if (!CheckPermissions()) {
            Log.d(TAG, "BAD PERMISSIONS");
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        MakeMap();

        //if (mLocationGranted) {
        //    StartLocationUpdates();
        //}
    }

    private void StartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "BAD START LOCATION UPDATES");
            return;
        }
        Log.d(TAG, "Starting Updates");
        if (mLocationCallback == null) {
            Log.d(TAG, "HERE222");
        }
        if (mLocationRequest == null) {
            Log.d(TAG, "HERE");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            Log.d(TAG, "Stopping Updates");
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void MakeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mLocationGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BAD REQUEST UPDATE");
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                FocusMap(new LatLng(location.getLatitude(), location.getLongitude()), 14);
            }
        }
    };

    private void FocusMap(LatLng latLng, float zoom) {
        MarkerOptions newMarker = new MarkerOptions();

        if (lastLocationMarker != null) {
            lastLocationMarker.remove();
            newMarker.position(latLng).title("You are now here");
            Toast.makeText(MapsActivity.this, "Updated Location", Toast.LENGTH_SHORT).show();
        }
        else {
            newMarker.position(latLng).title("Initial Position");
            Toast.makeText(MapsActivity.this, "Initial Position", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Focusing Map: " + latLng.latitude + ", " + latLng.longitude);
        lastLocationMarker = mMap.addMarker(newMarker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public boolean CheckServices() {
        Log.d(TAG, "Checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Services are working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "Fixable error occurred");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_CODE_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean CheckPermissions() {
        Log.d(TAG, "Checking Permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), fLocation) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), cLocation) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Valid Permissions");
                mLocationGranted = true;
                return true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "In Permission Results...");
        mLocationGranted = false;

        switch(requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "PERMISSION FAILED");
                            mLocationGranted = false;
                            return;
                        }
                    }
                    Log.d(TAG, "Permission Granted");
                    mLocationGranted = true;
                    MakeMap();
                }
            }
        }
    }



    //////////////////////////////////////
    // EVERYTHING PAST THIS IS NOT USED //
    //////////////////////////////////////





    /**




    private void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }




     private void GetLocation() {
     Log.d(TAG, "Getting Current Location");
     //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

     try {
     if (mLocationGranted) {
     final Task location = mFusedLocationClient.getLastLocation();
     location.addOnCompleteListener(new OnCompleteListener() {
    @Override
    public void onComplete(@NonNull Task task) {
    if (task.isSuccessful()) {
    Log.d(TAG, "Found Location!");
    Location currentLocation = (Location) task.getResult();
    FocusMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14f);

    } else {
    Log.d(TAG, "NULL LOCATION");
    Toast.makeText(MapsActivity.this, "NULL LOCATION", Toast.LENGTH_SHORT).show();

    }
    }
    });
    }

        } catch (SecurityException e) {
                Log.d(TAG, "Security Exception: " + e.getMessage());
                }
                }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        // ...
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }

        // ...

        // Update UI to match restored state
        //updateUI();
    }*/





    /*updateValuesFromBundle(savedInstanceState);


     mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
     // TODO: Consider calling
     //    ActivityCompat#requestPermissions
     // here to request the missing permissions, and then overriding
     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
     //                                          int[] grantResults)
     // to handle the case where the user grants the permission. See the documentation
     // for ActivityCompat#requestPermissions for more details.
     System.out.println("Requesting Permissions");
     ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
     return;
     }
     mFusedLocationClient.getLastLocation()
     .addOnSuccessListener(this, new OnSuccessListener<Location>() {
    @Override
    public void onSuccess(Location location) {
    // Got last known location. In some rare situations this can be null.

    if (location != null) {
    // Logic to handle location object
    //LatLng lastPos = new LatLng(location.getLatitude(), location.getLongitude());
    //mMap.addMarker(new MarkerOptions().position(lastPos).title("LastPos"));
    //mMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));

    //mCurrentLocation = location;
    //LatLng lastPos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    //mMap.addMarker(new MarkerOptions().position(lastPos).title("LastPos"));
    //mMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
    System.out.println("GET Last Location");
    } else {
    LatLng sydney = new LatLng(-34, 151);
    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14));
    }
    }

    });


     CreateLocationRequest();

     LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
     //.addLocationRequest(locRequest);

     SettingsClient client = LocationServices.getSettingsClient(this);
     Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

     task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
    @Override
    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
    // All location settings are satisfied. The client can initialize
    // location requests here.
    System.out.println("Location Settings Satisfied");
    mRequestingLocationUpdates = true;
    startLocationUpdates();
    }
    });

     task.addOnFailureListener(this, new OnFailureListener() {
    @Override
    public void onFailure(@NonNull Exception e) {
    if (e instanceof ResolvableApiException) {
    // Location settings are not satisfied, but this can be fixed
    // by showing the user a dialog.
    System.out.println("BAD Location Settings");
    try {
    // Show the dialog by calling startResolutionForResult(),
    // and check the result in onActivityResult().
    ResolvableApiException resolvable = (ResolvableApiException) e;
    resolvable.startResolutionForResult(MapsActivity.this,
    1);
    } catch (IntentSender.SendIntentException sendEx) {
    // Ignore the error.
    }
    }
    }
    });


     mLocationCallback = new LocationCallback() {
    @Override
    public void onLocationResult(LocationResult locationResult) {
    if (locationResult == null) {
    System.out.println("CallBack NULL");
    return;
    }
    for (Location location : locationResult.getLocations()) {
    // Update UI with location data
    System.out.println("LOOPING");


    //LatLng lastPos = new LatLng(location.getLatitude(), location.getLongitude());
    //mMap.addMarker(new MarkerOptions().position(lastPos).title("callback"));
    //mMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
    }
    }

    ;
    };
     */


}
