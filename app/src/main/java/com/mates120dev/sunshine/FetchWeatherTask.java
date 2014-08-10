package com.mates120dev.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.mates120dev.sunshine.data.WeatherContract;
import com.mates120dev.sunshine.data.WeatherContract.WeatherEntry;
import com.mates120dev.sunshine.data.WeatherContract.LocationEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mContext = context;
        mForecastAdapter = forecastAdapter;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
    * so for convenience we're breaking it out into its own method now.
    */
    private String getReadableDateString(long time){
// Because the API returns a unix timestamp (measured in seconds),
// it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

// These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);

        long locationID = addLocation(cityName);
        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {

            String day;
            String description;
            String highAndLow;


            JSONObject dayForecast = weatherArray.getJSONObject(i);


            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);

// description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

// Temperatures are in a child object called "temp". Try not to name variables
// "temp" when working with temperature. It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationID);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;

    }

    private long addLocation(String locationSettings, String cityName)
    {
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_POSTAL_CODE, locationSettings);
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_NAME, cityName);
        Uri insertUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
        return ContentUris.parseId(insertUri);
    }

    @Override
    protected String[] doInBackground(String... params) {

// If there's no zip code, there's nothing to look up. Verify size of params.
        if (params.length == 0) {
            return null;
        }

// These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

// Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
// Construct the URL for the OpenWeatherMap query
// Possible parameters are avaiable at OWM's forecast API page, at
// http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());

// Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

// Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
// Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
// But it does make debugging a *lot* easier if you print out the completed
// buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
// Stream was empty. No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
// If the code didn't successfully get the weather data, there's no point in attemping
// to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getWeatherDataFromJson(forecastJsonStr, numDays);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

// This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            mForecastAdapter.clear();
            for(String dayForecastStr : result) {
                mForecastAdapter.add(dayForecastStr);
            }
// New data is back from the server. Hooray!
        }
    }
}