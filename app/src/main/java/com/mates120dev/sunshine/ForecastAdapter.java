package com.mates120dev.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by eugene on 8/17/14.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    boolean useTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return useTodayLayout && position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public void setUseTodayLayout(boolean use)
    {
        useTodayLayout = use;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_item_forecast;
        if (viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder vh = new ViewHolder(view);
        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vh = (ViewHolder) view.getTag();
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        int viewType = getItemViewType(cursor.getPosition());
        int resId = -1;
        if (viewType == VIEW_TYPE_TODAY)
            resId = Utility.getArtResourceForWeatherCondition(weatherId);
        else
            resId = Utility.getIconResourceForWeatherCondition(weatherId);
        if (resId < 0)
            vh.iconView.setVisibility(View.INVISIBLE);
        else
        {
            vh.iconView.setImageResource(resId);
            vh.iconView.setVisibility(View.VISIBLE);
        }
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        vh.dateView.setText(Utility.getFriendlyDayString(context, dateString));
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        vh.descriptionView.setText(description);
        boolean isMetric = Utility.isMetric(context);
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        vh.highTempView.setText(Utility.formatTemperature(context, high, isMetric));
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        vh.lowTempView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.image);
            dateView = (TextView) view.findViewById(R.id.textDate);
            descriptionView = (TextView) view.findViewById(R.id.textForecast);
            highTempView = (TextView) view.findViewById(R.id.textHigh);
            lowTempView = (TextView) view.findViewById(R.id.textLow);
        }
    }
}
