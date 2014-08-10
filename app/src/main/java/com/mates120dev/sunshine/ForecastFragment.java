package com.mates120dev.sunshine;

/**
 * Created by eugene on 7/26/14.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mates120dev.sunshine.data.WeatherContract;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    ArrayAdapter<String> forecastAdapter;
    List<String> weekForecast;
    ListView listForecast;
    private static final String TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_forecast, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_refresh: {
                updateWeather();
                return true;
            }
            case R.id.action_show_location:
            {
                String location = getLocationFromPrefs();
                Uri locationUri = Uri.parse("geo:0,0?").buildUpon()
                        .appendQueryParameter("q", location)
                        .build();
                Intent intent = new Intent(Intent.ACTION_VIEW, locationUri);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
                else
                    Log.d(TAG, "No app to show map installed");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateWeather()
    {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity(), forecastAdapter);
        String location = getLocationFromPrefs();
        fetchWeatherTask.execute(location);
    }

    private String getLocationFromPrefs()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        return location;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);
        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.textForecast, new ArrayList<String>());
        listForecast = (ListView) rootView.findViewById(R.id.listViewForecast);
        listForecast.setAdapter(forecastAdapter);

        listForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = forecastAdapter.getItem(position);
                Intent intent = new Intent(ForecastFragment.this.getActivity(), ForecastDetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });
        return rootView;
    }



    private void updateViews(String[] weatherData)
    {
        forecastAdapter.clear();
        if (weatherData == null)
            return;
        weekForecast = new ArrayList<String>(Arrays.asList(weatherData));
        for (String f: weekForecast)
            forecastAdapter.add(f);
//        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.textForecast, weekForecast);
//        listForecast.setAdapter(forecastAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_POSTAL_CODE
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    private static final int FORECAST_LOADER = 0;
    private String mLocation;


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String startDate = WeatherContract.getDbDateString(new Date());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        String location = getLocationFromPrefs();
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}