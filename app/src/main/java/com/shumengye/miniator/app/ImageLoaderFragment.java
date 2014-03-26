package com.shumengye.miniator.app;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LruCache;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by shye on 25/03/14.
 */
public class ImageLoaderFragment extends Fragment {
    public static final String TAG = "ImageLoaderFragment";
    public static final String IMAGE_KEY = "MiniatorImage";

    private WeakReference<ImageDownloadAsyncTask> asyncTaskWeakRef;
    private LruCache mMemoryCache;
    OnImageLoadListener mCallbackActivity;

    public interface OnImageLoadListener {
        public void onPreImageLoad(Integer progress);
        public void onUpdateImageLoad(Integer progress);
        public void showBitmap(Bitmap bitmap);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Ensure the container activity has implemented callback interface
        try {
            mCallbackActivity = (OnImageLoadListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImageLoadListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment state
        setRetainInstance(true);

        // Create memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        this.mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
    }

    public void loadBitmap(String url, Integer targetWidth, Integer targetHeight) {
        // Return image from cache if possible
        final Bitmap bitmap = getBitmapFromMemCache(IMAGE_KEY);
        if (bitmap != null) {
            mCallbackActivity = (OnImageLoadListener) getActivity();
            mCallbackActivity.showBitmap(bitmap);
        }
        // Load bitmap asynchronously
        else if (!isAsyncTaskPendingOrRunning()) {
            ImageDownloadAsyncTask asyncTask = new ImageDownloadAsyncTask(this, url);
            this.asyncTaskWeakRef = new WeakReference<ImageDownloadAsyncTask>(asyncTask);
            asyncTask.execute(targetWidth, targetHeight);
        }
    }

    public boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskWeakRef != null &&
                this.asyncTaskWeakRef.get() != null &&
                !this.asyncTaskWeakRef.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        int size = (bitmap.getRowBytes() * bitmap.getHeight());
        if (getBitmapFromMemCache(key) == null && size > 8) {
            this.mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if (this.mMemoryCache != null && key!=null) {
            Bitmap bm = (Bitmap) this.mMemoryCache.get(key);
            return bm;
        }
        return null;
    }

    /**
     * ASyncTask for downloading image
     */
    private static class ImageDownloadAsyncTask extends AsyncTask<Integer, Integer, Bitmap> {
        private WeakReference<ImageLoaderFragment> fragmentWeakRef;
        private String url;
        private Bitmap bitmap;
        private int targetWidth;
        private int targetHeight;

        private ImageDownloadAsyncTask (ImageLoaderFragment fragment, String url) {
            this.fragmentWeakRef = new WeakReference<ImageLoaderFragment>(fragment);
            this.url = url;
        }

        @Override
        protected void onPostExecute(Bitmap downloadedBitmap) {
            super.onPostExecute(downloadedBitmap);
            if (this.fragmentWeakRef.get() != null) {
                ImageLoaderFragment fragment = this.fragmentWeakRef.get();
                if (fragment != null) {
                    fragment.mCallbackActivity = (OnImageLoadListener) fragment.getActivity();
                    fragment.mCallbackActivity.showBitmap(downloadedBitmap);
                }
            }
        }

        protected void onProgressUpdate(Integer... progress) {

            if (this.fragmentWeakRef.get() != null) {
                ImageLoaderFragment fragment = this.fragmentWeakRef.get();
                if (fragment != null) {
                    fragment.mCallbackActivity = (OnImageLoadListener) fragment.getActivity();
                    fragment.mCallbackActivity.onUpdateImageLoad(progress[0]);
                }
            }
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            this.targetWidth = params[0];
            this.targetHeight = params[1];

            try {
                    HttpUriRequest request = new HttpGet(url);
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(request);

                    byte[] data = null;
                    InputStream is = response.getEntity().getContent();
                    int contentSize = (int) response.getEntity().getContentLength();
                    BufferedInputStream bis = new BufferedInputStream(is, 8192);

                    data = new byte[contentSize];
                    int bytesRead = 0;
                    int offset = 0;

                    while (bytesRead != -1 && offset < contentSize) {
                        bytesRead = bis.read(data, offset, contentSize - offset);
                        offset += bytesRead;
                        // Publish % of download as progress
                        publishProgress((int) ((offset * 100) / contentSize));
                    }

                    // Decode with inJustDecodeBounds=true to check original dimensions
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, options);

                    // Calculate inSampleSize for down sampling size
                    options.inSampleSize = calculateImageDownSampleSize(options.outWidth, options.outHeight, this.targetWidth, this.targetHeight);
                    options.inJustDecodeBounds = false;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                    // Save bitmap to memory cache
                    ImageLoaderFragment fragment = this.fragmentWeakRef.get();
                    if (fragment != null) {
                        fragment.addBitmapToMemoryCache(IMAGE_KEY, bitmap);
                    }
                    return bitmap;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Calculates down sampling size for bitmap based on target dimensions
         */
        private static int calculateImageDownSampleSize(int imageWidth, int imageHeight, int targetWidth, int targetHeight) {
            int downSampleSize = 1;

            if (imageWidth > targetWidth || imageHeight > targetHeight) {

                final int halfHeight = imageHeight / 2;
                final int halfWidth = imageWidth / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / downSampleSize) > targetHeight
                        && (halfWidth / downSampleSize) > targetWidth) {
                    downSampleSize *= 2;
                }
            }

            return downSampleSize;
        }
    }
}
