package com.mates120dev.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mates120dev.sunshine.data.WeatherContract;

/**
 * Created by echirikov on 18.08.14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int FORECAST_LOADER = 0;
    private String mLocation;
    public static final String FORECAST_DATE_EXTRA = "forecast_date_extra";
    TextView monthView;
    TextView weekView;
    TextView forecastView;
    TextView humidityView;
    TextView windView;
    TextView pressureView;
    TextView highView;
    TextView lowView;
    ImageView image;
    String forecastDate;

    public static DetailFragment newInstance(String date) {
        DetailFragment f = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(FORECAST_DATE_EXTRA, date);
        f.setArguments(args);
        return f;
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
        Bundle extras = getArguments();
        if (extras != null)
        {
            forecastDate = extras.getString(FORECAST_DATE_EXTRA);
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forcast_detail, container, false);
        weekView = (TextView) rootView.findViewById(R.id.textWeekDay);
        monthView = (TextView) rootView.findViewById(R.id.textMonthDay);
        forecastView = (TextView) rootView.findViewById(R.id.textForecast);
        highView = (TextView) rootView.findViewById(R.id.textHigh);
        lowView = (TextView) rootView.findViewById(R.id.textLow);
        humidityView = (TextView) rootView.findViewById(R.id.textHumidity);
        windView = (TextView) rootView.findViewById(R.id.textWind);
        pressureView = (TextView) rootView.findViewById(R.id.textPressure);
        image = (ImageView) rootView.findViewById(R.id.image);
        return rootView;
    }

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int COL_WEATHER_DESC = 0;
    public static final int COL_WEATHER_MAX_TEMP = 1;
    public static final int COL_WEATHER_MIN_TEMP = 2;
    public static final int COL_WEATHER_HUMIDITY = 3;
    public static final int COL_WEATHER_WIND_SPEED = 4;
    public static final int COL_WEATHER_PRESSURE = 5;
    public static final int COL_WEATHER_DEGREES = 6;
    public static final int COL_WEATHER_ID = 7;

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
            weekView.setText(Utility.getDayName(getActivity(), forecastDate));
            monthView.setText(Utility.getFormattedMonthDay(getActivity(), forecastDate));
            forecastView.setText(cursor.getString(COL_WEATHER_DESC));
            highView.setText(Utility.formatTemperature(getActivity(),
                    cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
            lowView.setText(Utility.formatTemperature(getActivity(),
                    cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
            humidityView.setText(getString(R.string.format_humidity, cursor.getFloat(COL_WEATHER_HUMIDITY)));
            windView.setText(Utility.getFormattedWind(getActivity(), cursor.getFloat(COL_WEATHER_WIND_SPEED),
                    cursor.getFloat(COL_WEATHER_DEGREES)));
            pressureView.setText(getString(R.string.format_pressure, cursor.getFloat(COL_WEATHER_PRESSURE)));
            int imageResId = Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_ID));
            if (imageResId > 0) {
                image.setImageResource(imageResId);
                image.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        weekView.setText("");
        forecastView.setText("");
        highView.setText("");
        lowView.setText("");
        humidityView.setText("");
        windView.setText("");
        pressureView.setText("");
        monthView.setText("");
        image.setVisibility(View.INVISIBLE);
    }
}
