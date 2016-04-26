package com.example.android.popularmovie.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.android.popularmovie.Data.Movie;
import com.example.android.popularmovie.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by susanoo on 22/03/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie,
                    parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.grid_item_movie_image_view);
        Picasso.with(getContext()).load(movie.getPosterImgURL()).into(imageView);
        return convertView;

    }
}
