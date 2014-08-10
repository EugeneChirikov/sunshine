package com.mates120dev.sunshine.data;

import com.mates120dev.sunshine.data.WeatherContract.WeatherEntry;
import com.mates120dev.sunshine.data.WeatherContract.LocationEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by eugene on 8/2/14.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
// Create a table to hold locations. A location consists of the string supplied in the
// location setting, the city name, and the latitude and longitude

// TBD

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

// the ID of the location entry associated with this weather data
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

// Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

// To assure the application have just one weather entry per day
// per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LocationEntry.COLUMN_POSTAL_CODE + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                " UNIQUE (" +  LocationEntry.COLUMN_POSTAL_CODE + ", " +
                LocationEntry.COLUMN_LOCATION_NAME + ") ON CONFLICT IGNORE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
