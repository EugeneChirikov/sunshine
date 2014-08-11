package com.mates120dev.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mates120dev.sunshine.data.WeatherContract;

import java.util.Date;

public class ForecastDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.forecast_detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        String data = getData();
        if (data != null) {
            if (actionProvider != null)
                actionProvider.setShareIntent(createShareIntent(data));
        }
        return true;
    }

    private String getData()
    {
        String forecast = null;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
        {
            forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        return forecast;
    }

    private Intent createShareIntent(String data)
    {
        return new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, data + "#SunshineApp");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final int FORECAST_LOADER = 0;
        private String mLocation;
        private static final String FORECAST_DATE_EXTRA = "forecast_date_extra";
        TextView dateView;
        TextView forecastView;
        TextView highView;
        TextView lowView;
        String forecastDate;

        public PlaceholderFragment() {
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            {
                forecastDate = intent.getStringExtra(Intent.EXTRA_TEXT);

                getLoaderManager().initLoader(FORECAST_LOADER, null, this);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_forcast_detail, container, false);
            dateView = (TextView) rootView.findViewById(R.id.textDate);
            forecastView = (TextView) rootView.findViewById(R.id.textForecast);
            highView = (TextView) rootView.findViewById(R.id.textHigh);
            lowView = (TextView) rootView.findViewById(R.id.textLow);
            return rootView;
        }

        private static final String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        };

        public static final int COL_WEATHER_DESC = 0;
        public static final int COL_WEATHER_MAX_TEMP = 1;
        public static final int COL_WEATHER_MIN_TEMP = 2;

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " ASC";
            mLocation = Utility.getPreferredLocation(getActivity());
            Uri weatherForLocationAndDateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, forecastDate);

            return new CursorLoader(
                    getActivity(),
                    weatherForLocationAndDateUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if (cursor != null && cursor.moveToPosition(0)) {
                boolean isMetric = Utility.isMetric(getActivity());
                dateView.setText(Utility.formatDate(forecastDate));
                forecastView.setText(cursor.getString(COL_WEATHER_DESC));
                highView.setText(Utility.formatTemperature(
                        cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
                lowView.setText(Utility.formatTemperature(
                        cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            dateView.setText("");
            forecastView.setText("");
            highView.setText("");
            lowView.setText("");
        }
    }
}
