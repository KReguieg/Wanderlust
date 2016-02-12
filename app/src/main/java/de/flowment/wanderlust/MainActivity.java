package de.flowment.wanderlust;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import static android.widget.Toast.LENGTH_LONG;


/**
 * This class is our MainActivity, it holds a list and fills it with all tours in our db.
 */
public class MainActivity extends AppCompatActivity {

    // Get name of the activity for log statements.
    // Should be done in every activity at first.
    private static final String TAG = MainActivity.class.getName();
    /**
     * Database object to perform with database.
     */
    SQLiteDatabase db;
    ImageView headerImage;
    int counter;
    /**
     * Helper for SQLite Database.
     */
    private TourSQLiteHelper tourSQLiteHelper;
    private ArrayList<Tour> tourList = new ArrayList<>();
    private int mRndNumber;
    private LinearLayout tourListLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tourListLinearLayout = (LinearLayout) findViewById(R.id.tourListLinearLayout);
        fillListView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), TourActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * This method fills a vertical Scrollview with all entries of tours in the database.
     * It creates a nice card layout for each entry.
     */
    private void fillListView() {
        tourSQLiteHelper = new TourSQLiteHelper(MainActivity.this, "DBTour", null, 1);
        db = tourSQLiteHelper.getWritableDatabase();
        String[] fields = new String[]{"title", "time", "kiloMetersWalked", "pathToKMLFile"};

        // Adding Image on top
        headerImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headerImage.setLayoutParams(imageParams);
        headerImage.setAdjustViewBounds(true);
        headerImage.setScaleType(ImageView.ScaleType.FIT_XY);
        headerImage.setImageResource(R.drawable.wanderlust_header);
        tourListLinearLayout.addView(headerImage);

        headerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.counter++;
                if (counter >= 3) {
                    Toast.makeText(getApplicationContext(), "Forest feeling in " + (5 - counter), Toast.LENGTH_SHORT).show();
                }
                if (counter == 5) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=mbGee6NRuNY&"));
                    startActivity(browserIntent);
                    counter = 0;
                    // play sound
                }
            }
        });

        Cursor c = db.query("tour", fields, null, null, null, null, null);
        int id = 0;

        if (c.moveToFirst()) {
            do {
                Tour t = new Tour(id, c.getString(0), c.getInt(1), c.getDouble(2));
                tourList.add(t);
                id++;
            } while (c.moveToNext());
        }

        Integer[] preferredColors = new Integer[]{0xFFFFA726, 0xFF26A69A};
        Integer[] drawableIds = new Integer[]{
                R.drawable.ic_directions_walk_white_24dp, R.drawable.ic_directions_walk_white_24dp,
        };

        // Creating the layout for the list programmatically for each movie object in the movieList
        for (Tour t : tourList) {
            int lastRnd = mRndNumber;
            do {
                mRndNumber = new Random().nextInt(preferredColors.length);
            }
            while (lastRnd == mRndNumber);
            RelativeLayout rl = new RelativeLayout(this);
            LinearLayout.LayoutParams relativeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            relativeParams.setMargins(0, 0, 0, 25);
            rl.setLayoutParams(relativeParams);
            rl.requestLayout();
            rl.setBackgroundColor(preferredColors[mRndNumber]);
            rl.invalidate();
            ImageView tourImage = new ImageView(this);
            tourImage.setId(R.id.imageId);
            tourImage.setAdjustViewBounds(true);
            tourImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            RelativeLayout carrierRL = new RelativeLayout(this);
            RelativeLayout.LayoutParams carrierLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            carrierLP.addRule(RelativeLayout.RIGHT_OF, tourImage.getId());
            carrierLP.setMargins(5, 5, 5, 5);
            carrierRL.setLayoutParams(carrierLP);
            final int tripID = t.getTripID();

            carrierRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), ShowTourActivity.class);
                    intent.putExtra("id", tripID);
                    startActivity(intent);
                }
            });

            tourImage.setImageResource(drawableIds[mRndNumber]);
            tourImage.setPadding(20, 20, 20, 20);

            final TextView tourTitle = new TextView(this);
            tourTitle.setId(R.id.tourTitleId);
            tourTitle.setTextSize(28);
            tourTitle.setText(t.getTitle());

            TextView tourTimeInSeconds = new TextView(this);
            tourTimeInSeconds.setId(R.id.tourTimeInSecondsId);
            tourTimeInSeconds.setTextSize(24);
            tourTimeInSeconds.setText(String.valueOf(DateUtils.formatElapsedTime(t.getTimeInSeconds())));

            TextView tourKilometersWalked = new TextView(this);
            tourKilometersWalked.setId(R.id.tourKilometerWalkedId);
            tourKilometersWalked.setText(String.format("%.3f km", t.getKiloMetersWalked() / 1000.0));


            final ImageButton deleteButton = new ImageButton(this);
            deleteButton.setId(R.id.deleteButton);
            deleteButton.setImageResource(R.drawable.ic_delete_24dp);
            deleteButton.setBackgroundColor(Color.TRANSPARENT);
            RelativeLayout.LayoutParams btnLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            btnLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            btnLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnLP.addRule(RelativeLayout.ALIGN_PARENT_END);
            deleteButton.setLayoutParams(btnLP);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle(Html.fromHtml("<font color='#000000'>" + getString(R.string.titleDialog) + "</font>"));
                    Resources res = getResources();
                    String text = String.format(res.getString(R.string.messageDialog), tourTitle.getText().toString());

                    alert.setMessage(text);
                    alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (db != null) {
                                db.delete("tour", "title = ?", new String[]{tourTitle.getText().toString()});
                            }
                            View parent = (View) deleteButton.getParent().getParent();
                            parent.setVisibility(View.GONE);
                        }
                    });
                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            });

            TextView[] textViews = new TextView[]{tourTitle, tourTimeInSeconds, tourKilometersWalked};
            Integer[] textViewIds = new Integer[]{tourTitle.getId(), tourTimeInSeconds.getId(), tourKilometersWalked.getId()};

            rl.addView(tourImage);

            carrierRL.addView(tourTitle);
            for (int i = 0; i < textViews.length - 1; i++) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.BELOW, textViewIds[i]);
                carrierRL.addView(textViews[i + 1], layoutParams);
            }
            carrierRL.addView(deleteButton);
            rl.addView(carrierRL);
            tourListLinearLayout.addView(rl);
        }
    }
}
