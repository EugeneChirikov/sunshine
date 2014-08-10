package com.mates120dev.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.mates120dev.sunshine.data.WeatherContract;
import com.mates120dev.sunshine.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by eugene on 8/10/14.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();
    public static String TEST_CITY_NAME = "North Pole";
    public static String TEST_POSTAL_CODE = "99705";

    public void testDeleteDB() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    private ContentValues getLocationContentValues()
    {
        String testLocationSetting = TEST_POSTAL_CODE;
        String testCityName = TEST_CITY_NAME;
        double testLatitude = 64.7488;
        double testLongitude = -147.353;
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_POSTAL_CODE, testLocationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_NAME, testCityName);
//        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
//        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);
        return values;
    }

    private ContentValues getWeatherContentValues(long locationRowId)
    {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public void testInsertReadProvider() {
        long locationRowId;
        ContentValues values = getLocationContentValues();
        Uri locInsertUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
        locationRowId = ContentUris.parseId(locInsertUri);
        Log.d(LOG_TAG, "New location row id: " + locationRowId);

        Cursor cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        validateCursor(cursor, values);

        cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null);
        validateCursor(cursor, values);

        values = getWeatherContentValues(locationRowId);
        Uri insertUri = mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, values);
        long weatherRowId = ContentUris.parseId(insertUri);
        Log.d(LOG_TAG, "New weather row id: " + weatherRowId);

        cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        validateCursor(cursor, values);

        cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.buildWeatherLocation(TEST_POSTAL_CODE),
                null,
                null,
                null,
                null);
        validateCursor(cursor, values);
    }

    public void testGetType()
    {
        String type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.CONTENT_URI);
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.buildWeatherLocation(testLocation));
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry
                .buildWeatherLocationWithDate(testLocation, testDate));
        assertEquals(WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.CONTENT_URI);
        assertEquals(WeatherContract.LocationEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.buildLocationUri(1L));
        assertEquals(WeatherContract.LocationEntry.CONTENT_ITEM_TYPE, type);
    }

}