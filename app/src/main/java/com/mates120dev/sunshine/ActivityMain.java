package com.mates120dev.sunshine;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ActivityMain extends ActionBarActivity implements Callback {

    ViewServer viewServer = null;
    boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null)
        {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        }
        else
            mTwoPane = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewServer = ViewServer.get(this);
        try {
            viewServer.start();
            viewServer.addWindow(this);
        } catch (IOException e) {
            e.printStackTrace();
            viewServer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewServer != null)
            viewServer.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            String tag = "DETAIL_FRAGMENT";
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.weather_detail_container,  DetailFragment.newInstance(date));
//            ft.addToBackStack(tag);
            ft.commit();
        }
        else {
            Intent intent = new Intent(this, ForecastDetailActivity.class)
                    .putExtra(DetailFragment.FORECAST_DATE_EXTRA, date);
            startActivity(intent);
        }
    }
}
