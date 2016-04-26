package com.example.android.popularmovie;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovie.Adapters.ReviewAdapter;
import com.example.android.popularmovie.Adapters.TrailerAdapter;
import com.example.android.popularmovie.Data.Movie;
import com.example.android.popularmovie.Data.Review;
import com.example.android.popularmovie.Data.Trailer;
import com.example.android.popularmovie.Database.MoviesContract;
import com.linearlistview.LinearListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by susanoo on 25/03/16.
 */
public class DetailFragment extends Fragment {

    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_MOVIE = "DETAIL_MOVIE";

    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;
    private Movie movie;

    public DetailFragment() {
        //setHasOptionsMenu(true);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            movie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        reviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
        trailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(/*Intent.EXTRA_TEXT*/DetailFragment.DETAIL_MOVIE)) {
            movie = intent.getExtras().getParcelable(DetailFragment.DETAIL_MOVIE);
        }
        if(movie != null){
            ImageView imageView = (ImageView)rootView.findViewById(R.id.backdrop_img);

            Picasso.with(getContext()).load(movie.getBackdropImg()).into(imageView);
            final TextView movieTitle = (TextView)rootView.findViewById(R.id.detail_movie_title);
            movieTitle.setText(movie.getOriginalTitle());


            TextView overviewTextView = (TextView) rootView.findViewById(R.id.overview_text);
            overviewTextView.setText(movie.getOverview());


            TextView releaseDateTextView = (TextView) rootView.findViewById(R.id.detail_release_date);
            releaseDateTextView.setText(movie.getReleaseDate());

            TextView voteRateTextView = (TextView) rootView.findViewById(R.id.detail_vote_average);
            voteRateTextView.setText(movie.getVoteAverage() + "/10");

            LinearListView reviews = (LinearListView) rootView.findViewById(R.id.detail_reviews);
            reviews.setAdapter(reviewAdapter);

            final LinearListView trailers = (LinearListView) rootView.findViewById(R.id.detail_trailers);
            trailers.setAdapter(trailerAdapter);
            trailers.setOnItemClickListener(new LinearListView.OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView linearListView, View view,
                                        int position, long id) {
                    Trailer trailer = trailerAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                    startActivity(intent);
                }
            });
            Switch favoriteSwitch = (Switch) rootView.findViewById(R.id.favorite_switch);

            final Uri movieUri =
                    MoviesContract.MovieEntry.buildMoviesUri(Long.parseLong(movie.getId()));
            final Cursor cursor =
                    getActivity().getContentResolver().query(movieUri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    favoriteSwitch.setChecked(true);
                }
                cursor.close();
            }
            favoriteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        final long movieId = Long.parseLong(movie.getId());
                        final Uri movieUri = MoviesContract.MovieEntry.buildMoviesUri(movieId);
                        final Cursor cursor =
                                getActivity().getContentResolver().query(movieUri, null, null, null, null);
                        if (cursor != null) {

                            if (!cursor.moveToFirst()) {
                                // nothing in database, add it
                                final ContentValues movieValues = new ContentValues();

                                movieValues.put(MoviesContract.MovieEntry.COLUMN_ID, movie.getId());
                                movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, movie.getOriginalTitle());
                                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                                movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL,
                                        movie.getPosterImgURL()+","+movie.getBackdropImg()+",");

                                movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
                                        movie.getReleaseDate());
                                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                                        movie.getVoteAverage());

                                getActivity().getContentResolver()
                                        .insert(MoviesContract.MovieEntry.CONTENT_URI, movieValues);
                            }
                            cursor.close();
                            Toast.makeText(getContext(),"favorited", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        // remove from database
                        getActivity().getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,
                                MoviesContract.MovieEntry.COLUMN_ID + "=?",
                                new String[] { movie.getId() });
                        Toast.makeText(getContext(),"deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        return rootView;
    }





    @Override
    public void onStart() {
        super.onStart();

        if(movie != null){
            new FetchReviewsTask().execute(movie.getId());
            new FetchTrailersTask().execute(movie.getId());
        }
    }


    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            List<Review> results = new ArrayList<>();

            for(int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                String id = review.getString("id");
                String author = review.getString("author");
                String content = review.getString("content");
                results.add(new Review(id, author, content));
            }

            return results;
        }

        @Override
        protected List<Review> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
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
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null) {
                if (reviews.size() > 0) {
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();
                        for (Review review : reviews) {
                            reviewAdapter.add(review);
                        }
                    }
                }
                else{
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();

                            reviewAdapter.add(new Review(null,"There is no reviews", null));
                    }
                }
            }
        }
    }
    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Trailer> results = new ArrayList<>();

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);

                // Only show Trailers which are on Youtube
                if (trailer.getString("site").equals("YouTube")) {
                    Trailer trailerModel = new Trailer(trailer.getString("id"),
                            trailer.getString("key"),
                            trailer.getString("name"),
                            trailer.getString("site"),
                            trailer.getString("type"));
                    results.add(trailerModel);
                }
            }

            return results;

        }

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
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
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    if (trailerAdapter != null) {
                        trailerAdapter.clear();
                        for (Trailer trailer : trailers) {
                            trailerAdapter.add(trailer);
                        }
                    }
                }
            }
        }
    }

}
