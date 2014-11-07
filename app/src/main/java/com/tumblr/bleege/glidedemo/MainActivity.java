package com.tumblr.bleege.glidedemo;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private final static String markerURL = "http://api.tiles.mapbox.com/v3/marker/pin-l-marker+B70101@2x.png";
    private final static String tileURL = "http://api.tiles.mapbox.com/v3/examples.map-zr0njcqy/0/0/0.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clearCache();

        ImageView imageView = (ImageView)findViewById(R.id.makiIcon);
        Glide.with(this).load(markerURL).into(imageView);

        loadImageFromDiskCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.actionLoadTileToCache) {
            // Download Image To Cache
            Toast.makeText(this, "Begin Image Download to cache", Toast.LENGTH_SHORT).show();
            DownloadTileToCacheTask task = new DownloadTileToCacheTask();
            task.execute(tileURL);
            //task.cancel(true);
            return true;
        } else if (id == R.id.actionClearDiskCache) {
            Toast.makeText(this, "Clear Disk Cache", Toast.LENGTH_SHORT).show();
            clearCache();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleLoadCacheTileButton(View view) {
        Toast.makeText(this, "Try to load Image from Disk Cache", Toast.LENGTH_LONG).show();
        loadImageFromDiskCache();
    }

    private void loadImageFromDiskCache() {
        final ImageView tileView = (ImageView)findViewById(R.id.cacheTileImageView);
        // Loads image from network if not found in disk cache
//        Glide.with(this).load(tileURL).diskCacheStrategy(DiskCacheStrategy.ALL).into(tileView);
        Glide.with(this).load(tileURL).diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                Log.i(TAG, "Listener onException: " + e.toString());
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Log.i(TAG, "onResourceReady with resource = " + resource);
                Log.i(TAG, "onResourceReady from memory cache = " + isFromMemoryCache);
                return false;
            }
        })
        .into(new SimpleTarget<GlideDrawable>(256, 256) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                Log.i(TAG, "GlideDrawalble = '" + resource + "'");
                tileView.setImageDrawable(resource.getCurrent());
            }
        });
    }

    private class DownloadTileToCacheTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            FutureTarget<File> future = Glide.with(getApplicationContext())
                    .load(params[0])
                    .downloadOnly(256, 256);

            File file = null;
            try {
                file = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return file;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getApplicationContext(), "CANCELLED!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            Log.i(TAG, "onPostExcecute with file = '" + file + "'");
            Toast.makeText(getApplicationContext(), "Finished downloading file to cache", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearCache() {
        Log.w(TAG, "clearing cache");
        Glide.get(this).clearMemory();
        File cacheDir = Glide.getPhotoCacheDir(this);
        if (cacheDir.isDirectory()) {
            Log.w(TAG, "Number of files to clear from cache: " + cacheDir.listFiles().length);
            for (File child : cacheDir.listFiles()) {
                if (!child.delete()) {
                    Log.w(TAG, "cannot delete: " + child);
                }
            }
        }
    }
}
