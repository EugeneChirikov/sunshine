package com.mates120dev.sunshine;

import android.content.Intent;
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
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_forcast_detail, container, false);
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            {
                String forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView detailText = (TextView) rootView.findViewById(R.id.detail_text);
                detailText.setText(forecast);
            }
            return rootView;
        }
    }
}
