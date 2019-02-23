package com.spaceshipfreehold.gpsmockery;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText mLatitudeEditText;
    EditText mLongitudeEditText;
    Button mSpoofButton;
    org.osmdroid.views.MapView mMap;

    View.OnClickListener mSpoofButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spoof();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // According to OSM,
        Context context = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        setContentView(R.layout.activity_main);

        checkPermissions();

        mLatitudeEditText = findViewById(R.id.latitude);
        mLongitudeEditText = findViewById(R.id.longitude);
        mSpoofButton = findViewById(R.id.spoof);
        mSpoofButton.setOnClickListener(mSpoofButtonOnClickListener);

        mMap = findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMap.onResume();
    }

    private void checkPermissions(){
        // Check permissions
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            ArrayList<String> permissions = new ArrayList<String>();
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
            permissions.add(Manifest.permission.INTERNET);

            for(String permission : permissions){
                int res = this.checkCallingOrSelfPermission(permission);
                if (res != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Missing permission: " + permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void spoof() {

        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            String mocLocationProvider = LocationManager.GPS_PROVIDER;//lm.getBestProvider( criteria, true );

            if (mocLocationProvider == null) {
                Toast.makeText(getApplicationContext(), "No location provider found!", Toast.LENGTH_SHORT).show();
                return;
            }

            lm.addTestProvider(mocLocationProvider, false, false,
                    false, false, true, true, true, 0, 5);

            lm.setTestProviderEnabled(mocLocationProvider, true);

            Location loc = new Location(mocLocationProvider);
            Location mockLocation = new Location(mocLocationProvider); // a string
            mockLocation.setLatitude(Double.valueOf(mLatitudeEditText.getText().toString()));
            mockLocation.setLongitude(Double.valueOf(mLongitudeEditText.getText().toString()));
            mockLocation.setAltitude(loc.getAltitude());
            mockLocation.setTime(System.currentTimeMillis());
            mockLocation.setAccuracy(1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            lm.setTestProviderLocation(mocLocationProvider, mockLocation);
            Toast.makeText(getApplicationContext(), "Location set", Toast.LENGTH_SHORT).show();

        }
        catch(Exception e){
            Toast.makeText(this,"Something Went Wrong", Toast.LENGTH_SHORT).show();
        }
    }

}
