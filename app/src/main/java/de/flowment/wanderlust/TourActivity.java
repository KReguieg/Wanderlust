package de.flowment.wanderlust;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TourActivity extends AppCompatActivity {

    TextView speedTextView;
    TextView timeTextView;
    TextView distanceTextView;
    TextView heightTextView;
    SQLiteDatabase db;
    private Location mLocation;
    private Chronometer chronometer;
    private boolean recordStarted;
    private KmlWriter kmlWriter;
    private TripSQLiteHelper tripSQLiteHelper;
    private File pathToKMLFile;
    private FloatingActionButton fab;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tripSQLiteHelper = new TripSQLiteHelper(TourActivity.this, "DBTrip", null, 1);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        try {
            pathToKMLFile = new File("/sdcard/" +
                    String.valueOf(System.currentTimeMillis() / 1000) +
                    ".kml");
            kmlWriter = new KmlWriter(new FileOutputStream(pathToKMLFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        speedTextView = (TextView) findViewById(R.id.speedTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        heightTextView = (TextView) findViewById(R.id.heightTextView);
        chronometer = (Chronometer) findViewById(R.id.chronometer);

        recordStarted = false;
        firstTime = true;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Started Walk= " + recordStarted, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (recordStarted) {
                    fab.setImageResource(R.drawable.ic_play_arrow_24dp);
                    recordStarted = false;
                } else {
                    fab.setImageResource(R.drawable.ic_stop_24dp);
                    if (firstTime) {
                        InitializeWalk();
                        firstTime = false;
                    }
                    recordStarted = true;
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Thanks for Location Access", Toast.LENGTH_SHORT).show();
                    InitializeWalk();
                } else {
                    Toast.makeText(this, "You did not allow to access your current location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void InitializeWalk() {
        chronometer.start();
        chronometer.setBase(SystemClock.elapsedRealtime());
        getLocation();
    }

    /**
     *
     */
    private void getLocation() {
        LocationManager locationManager = (LocationManager) TourActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
                speedTextView.setText("Speed: \n\n" + location.getSpeed() * 3.6 + " km/h");
                distanceTextView.setText("Distance: \n\n");
                heightTextView.setText("Height: \n\n" + location.getAltitude() + " m");
                if (recordStarted && kmlWriter != null)
                    kmlWriter.pushLocation(mLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("Status= " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                TourActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

}
