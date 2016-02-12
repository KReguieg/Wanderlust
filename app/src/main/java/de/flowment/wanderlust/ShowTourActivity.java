package de.flowment.wanderlust;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShowTourActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = ShowTourActivity.class.getName();
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
     * Helper for SQLite Database.
     */
    private TourSQLiteHelper tourSQLiteHelper;
    /**
     * Holds the path to the kml file.
     */
    private File pathToKMLFile;
    private boolean firstTime;
    private GoogleMap googleMap;

    /**
     * A Tour object, holding the data of a tour.
     */
    private Tour mTour;
    private int tourid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtour);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tourSQLiteHelper = new TourSQLiteHelper(ShowTourActivity.this, "DBTour", null, 1);
        tourid = getIntent().getIntExtra("id", 0);

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

        SupportMapFragment fm = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.mapfrag);

        fm.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    String[] fields = new String[]{"title", "time", "kiloMetersWalked", "pathToKMLFile"};

    private void loadTourFromDatabase() {
        db = tourSQLiteHelper.getReadableDatabase();
        long numEntries = DatabaseUtils.queryNumEntries(db, "tour");
        Cursor c = db.query("tour", fields, null, null, null, null, null);
        c.moveToPosition(tourid);
        //Tour t = new Tour(tourid, c.getString(0), c.getInt(2), c.getDouble(3));
        String path = c.getString(3);

        String title = c.getString(0);
        //int time = t.getTimeInSeconds();
        distanceTextView.setText(c.getInt(2) + " m");
        timeTextView.setText(c.getString(1));
        File file = new File(path);
        InputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            LatLng latLng = getLL(fileInputStream);
            fileInputStream = new FileInputStream(file);
            KmlLayer layer = new KmlLayer(googleMap, fileInputStream, getApplicationContext());
            layer.addLayerToMap();

            //double lon = Double.parseDouble(layer.getPlacemarks().iterator().next().getProperty("longitude"));
            //Toast.makeText(this, latLng.latitude+ " / " + latLng.longitude, Toast.LENGTH_SHORT).show();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private LatLng getLL(InputStream fileInputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
        double lon = 0;
        double lat = 0;
        try {
            String line;

            String longitude = "<longitude>";
            String latitude = "<latitude>";

            int digits = 14;
            while ((line = br.readLine()) != null)
            {
                if(line.startsWith(longitude))
                    lon = Double.parseDouble(line.substring(longitude.length(), line.indexOf("</")));
                if(line.startsWith(latitude))
                    lat = Double.parseDouble(line.substring(latitude.length(), line.indexOf("</")));
                if(lat != 0 && lon != 0)
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            br.close();
            fileInputStream.close();
        }
        return new LatLng(lat,lon);
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //Toast.makeText(getApplicationContext(),"Map ready", Toast.LENGTH_SHORT).show();

        loadTourFromDatabase();
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
