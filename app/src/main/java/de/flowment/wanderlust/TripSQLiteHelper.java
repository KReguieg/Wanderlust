package de.flowment.wanderlust;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Khaled Reguieg (s57532) <a href="mailto:Khaled.Reguieg@gmail.com">Khaled Reguieg, Khaled.Reguieg@gmail.com</a> on 07.01.2016.
 * This class builds the frame for the database tables and drops if it already exists.
 */
public class TripSQLiteHelper extends SQLiteOpenHelper {
    String createTripSQL = "CREATE TABLE trip (tripID INTEGER PRIMARY KEY, title TEXT, time INTEGER, kiloMetersWalked REAL, pathToKMLFile TEXT)";

    public TripSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTripSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL(createTripSQL);
    }
}
