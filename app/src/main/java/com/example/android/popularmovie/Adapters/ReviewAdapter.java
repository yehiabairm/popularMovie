package com.example.android.popularmovie.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovie.Data.Review;
import com.example.android.popularmovie.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by susanoo on 19/04/16.
 */
public class ReviewAdapter extends ArrayAdapter<Review> {
    public ReviewAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Review review = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_review_item,
                    parent, false);
        }

        TextView reviewAuthor = (TextView) convertView.findViewById(R.id.review_author);
        reviewAuthor.setText(review.getAuthor());

        TextView reviewContent = (TextView) convertView.findViewById(R.id.review_content);
        reviewContent.setText(review.getContent());
        return convertView;
    }

}
