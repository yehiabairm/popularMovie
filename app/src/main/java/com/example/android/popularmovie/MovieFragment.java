package com.example.android.popularmovie;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popularmovie.Adapters.MovieAdapter;
import com.example.android.popularmovie.Data.Movie;
import com.example.android.popularmovie.Database.MoviesContract;


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

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        void onItemSelected(Movie movie);
    }



    private ArrayList<Movie> movieList;

    private MovieAdapter movieAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || !savedInstanceState.containsKey("movies")){
            movieList = new ArrayList<Movie>();
        }
        else {
            movieList = savedInstanceState.getParcelableArrayList("movies");
        }
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_popular) {
            updateData("popular");
            return true;
        }
        if(id == R.id.action_top_rated){
            updateData("top_rated");
            return true;
        }
        if(id == R.id.action_favorite){

            final ContentResolver resolver = getActivity().getContentResolver();
            final Cursor cursor =
                    resolver.query(MoviesContract.MovieEntry.CONTENT_URI, null, null, null, null);

            final int COLUMN_ID = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_ID);
            final int COLUMN_OVERVIEW =
                    cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
            final int COLUMN_POSTER_URL =
                    cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER_URL);
            final int COLUMN_RELEASE_DATE =
                    cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
            final int COLUMN_TITLE = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
            final int COLUMN_VOTE_AVERAGE =
                    cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE);

                if (cursor.moveToFirst()) {
                    final Movie[] movies = new Movie[cursor.getCount()];
                    int count = 0;
                    while (!cursor.isAfterLast()) {

                        String pics = cursor.getString(COLUMN_POSTER_URL);
                        String a[] = pics.split(",");


                        final Movie movie = new Movie(cursor.getString(COLUMN_TITLE),
                                a[0],
                                cursor.getString(COLUMN_OVERVIEW),
                                cursor.getString(COLUMN_RELEASE_DATE),
                                cursor.getString(COLUMN_VOTE_AVERAGE),
                                a[1],
                                cursor.getString(COLUMN_ID)
                                );

                        movies[count] = movie;
                        count++;
                        cursor.moveToNext();
                    }
                    cursor.close();

                    movieAdapter.clear();
                    for (final Movie movie : movies) {
                        movieAdapter.add(movie);

                    }
                    movieAdapter.notifyDataSetChanged();
                }
            }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(movieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movieAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });

        return rootView;
    }

    private void updateData(String type){
        FetchMovieData fetchMovieData = new FetchMovieData();
        fetchMovieData.execute(type);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateData("popular");
    }

    public class FetchMovieData extends AsyncTask<String, Void, ArrayList<Movie>>{
        private final String LOG_TAG = FetchMovieData.class.getSimpleName();

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException{
            ArrayList<Movie> moviesData = new ArrayList<Movie>();

            final String RESULTS = "results";
            final String POSTER = "poster_path";
            final String OVERVIEW = "overview";
            final String REL_DATE = "release_date";
            final String TITLE = "title";
            final String VOTE_AVG = "vote_average";
            final String BACKDROP_IMG = "backdrop_path";
            final String ID = "id";
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            for(int i = 0; i < 20; i++){
                String movieTitle = movieArray.getJSONObject(i).getString(TITLE);
                String moviePoster = "http://image.tmdb.org/t/p/w500/" +
                        movieArray.getJSONObject(i).getString(POSTER);
                String movieOverview = movieArray.getJSONObject(i).getString(OVERVIEW);
                String movieRelDate = movieArray.getJSONObject(i).getString(REL_DATE);
                String movieVoteAvg = movieArray.getJSONObject(i).getString(VOTE_AVG);
                String backdropImg = "http://image.tmdb.org/t/p/w500/" +
                        movieArray.getJSONObject(i).getString(BACKDROP_IMG);
                String id = movieArray.getJSONObject(i).getString(ID);
                Movie movie = new Movie(movieTitle, moviePoster, movieOverview, movieRelDate,
                        movieVoteAvg, backdropImg, id);

                moviesData.add(i, movie);
            }

            return moviesData;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "?api_key=";
                final String API_KEY = getResources().getString(R.string.api_key);

                Uri builtUri = Uri.parse(MOVIE_BASE_URL + API_KEY).buildUpon().build();

                URL url = new URL(builtUri.toString());



                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
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
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            }catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                return getMovieDataFromJson(movieJsonStr);
            }catch (JSONException e){
                e.printStackTrace();
            }


            // This will only happen if there was an error getting or parsing the forecast.
            return null;

        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if(movies != null){
                movieAdapter.clear();
                for(Movie movie: movies){

                    movieAdapter.add(movie);
                }
            }
            movieList.addAll(movies);
        }
    }
}
