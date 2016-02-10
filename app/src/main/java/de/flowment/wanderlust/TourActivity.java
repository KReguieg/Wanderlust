package de.flowment.wanderlust;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class TourActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = TourActivity.class.getName();
    /**
     * EditText which allows creating a title.
     */
    EditText titleEditText;
    /**
     * TextView representing the current movement speed in km/h.
     */
    TextView speedTextView;
    /**
     * TextView representing the current time in the format xx:xx.
     */
    TextView timeTextView;
    /**
     * TextView representing the current distance walked in km/h.
     */
    TextView distanceTextView;
    /**
     * TextView representing the current height in m NN.
     */
    TextView heightTextView;
    /**
     * Database object to perform with database.
     */
    SQLiteDatabase db;
    int mDistanceWalked;
    ArrayList<Location> locationList;
    boolean first = true;
    /**
     * Field holding the location from the LocationListener
     */
    private Location mLocation;
    /**
     * Chronometer Object from view, counting the time a user takes for a walk.
     */
    private Chronometer chronometer;
    /**
     * Helper variable to determine whether a tour started or not.
     */
    private boolean recordStarted;
    /**
     * KmlWriter object for adding and writing kml objects to a .kml-file.
     */
    private KmlWriter kmlWriter;
    /**
     * Helper for SQLite Database.
     */
    private TourSQLiteHelper tourSQLiteHelper;
    /**
     * Holds the path to the kml file.
     */
    private File pathToKMLFile;
    /**
     * A Android Material Design FAB.
     */
    private FloatingActionButton fab;
    private boolean firstTime;
    private GoogleMap googleMap;

    /**
     * A Tour object, holding the data of a tour.
     */
    private Tour mTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tourSQLiteHelper = new TourSQLiteHelper(TourActivity.this, "DBTour", null, 1);
        try {
            pathToKMLFile = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/"
                    + String.valueOf(System.currentTimeMillis() / 1000)
                    + ".kml");
            kmlWriter = new KmlWriter(new FileOutputStream(pathToKMLFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        locationList = new ArrayList<>();
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        titleEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleEditText.setFocusable(true);
                titleEditText.setFocusableInTouchMode(true);
                titleEditText.setClickable(true);
            }
        });

        speedTextView = (TextView) findViewById(R.id.speedTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        heightTextView = (TextView) findViewById(R.id.heightTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        chronometer = (Chronometer) findViewById(R.id.chronometer);

        recordStarted = false;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Started Walk= " + recordStarted, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (recordStarted) {
                    stopTour();
                } else {
                    startTour();
                }
            }
        });

        //SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager()
        //        .findFragmentById(R.id.mapfrag);

        //fm.getMapAsync(this);
        //googleMap = fm.getMap();

        //googleMap.setMyLocationEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * This methods stops a tour and executes all necessary steps.
     */
    private void stopTour() {
        fab.setImageResource(R.drawable.ic_play_arrow_24dp);
        chronometer.stop();
        recordStarted = false;
        kmlWriter.writeKml();
        saveTourToDatabase();
    }

    /**
     * This method starts a Tour with all necessary steps.
     */
    private void startTour() {
        fab.setImageResource(R.drawable.ic_stop_24dp);
        recordStarted = true;
        chronometer.start();
        chronometer.setBase(SystemClock.elapsedRealtime());
        getLocation();
    }

    /**
     * Saves a Tour to the Database.
     */
    private void saveTourToDatabase() {
        db = tourSQLiteHelper.getWritableDatabase();
        long numEntries = DatabaseUtils.queryNumEntries(db, "tour");
        int tourId = (int) (numEntries + 1);
        String tourTitle;
        if (TextUtils.isEmpty(titleEditText.getText()))
            tourTitle = "Tour" + tourId;
        else
            tourTitle = titleEditText.getText().toString();
        int tourTime = (int) (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
        mTour = new Tour((int) (numEntries + 1), tourTitle, tourTime, mDistanceWalked);
        String title = mTour.getTitle();
        int time = mTour.getTimeInSeconds();
        double kilometersWalked = mTour.getKiloMetersWalked();
        Toast.makeText(TourActivity.this, String.valueOf(pathToKMLFile), LENGTH_LONG).show();
        if (db != null) {
            // preparing content values
            ContentValues newRecord = new ContentValues();
            newRecord.put("title", title);
            newRecord.put("time", time);
            newRecord.put("kilometersWalked", kilometersWalked);
            newRecord.put("pathToKMLFile", String.valueOf(pathToKMLFile));

            // Insert records
            db.insert("tour", null, newRecord);
            db.close();
        }
        Toast.makeText(TourActivity.this, "Your Tour \"" + title + "\" has been added.", LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Thanks for Location Access", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You did not allow to access your current location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * This method initializes the LocationManager and creates a LocationListener,
     * which implements all necessary methods.
     */
    private void getLocation() {
        LocationManager locationManager = (LocationManager) TourActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (first) mLocation = location;
                first = false;
                mDistanceWalked += location.distanceTo(mLocation);
                mLocation = location;
                locationList.add(location);
                speedTextView.setText(" " + location.getSpeed() * 3.6 + " km/h");
                distanceTextView.setText(" " + mDistanceWalked + "m");
                heightTextView.setText(" " + (int) location.getAltitude() + "m");
                timeTextView.setText(" " + chronometer.getText());
                if (recordStarted && kmlWriter != null)
                    kmlWriter.pushLocation(mLocation);

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

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
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                TourActivity.this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
    }
}
