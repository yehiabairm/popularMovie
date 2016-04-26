package com.example.android.popularmovie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by susanoo on 25/03/16.
 */
public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();

        }
    }

}
