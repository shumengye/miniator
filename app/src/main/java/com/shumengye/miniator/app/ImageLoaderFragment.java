package com.shumengye.miniator.app;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public static final String ImageKey= "Minion";

    private WeakReference<MyAsyncTask> asyncTaskWeakRef;
    private LruCache mMemoryCache;
    OnImageLoadListener mCallbackActivity;

    public interface OnImageLoadListener {
        public void onPreImageLoad(Integer progress);
        public void onUpdateImageLoad(Integer progress);
        public void showBitmap(Bitmap bitmap);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment state
        setRetainInstance(true);

        // Initiate memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        this.mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    public void loadImage(String url, Integer targetWidth, Integer targetHeight) {
        final Bitmap bitmap = getBitmapFromMemCache(ImageKey);

        if (bitmap != null) {

            mCallbackActivity = (OnImageLoadListener) getActivity();
            mCallbackActivity.showBitmap(bitmap);
        }
        else if (!isAsyncTaskPendingOrRunning()) {
            MyAsyncTask asyncTask = new MyAsyncTask(this, url);
            this.asyncTaskWeakRef = new WeakReference<MyAsyncTask>(asyncTask);
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
            Log.v("DEBUG","Saving bitmap to cache ");
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

    private static class MyAsyncTask extends AsyncTask<Integer, Integer, Boolean> {

        private WeakReference<ImageLoaderFragment> fragmentWeakRef;
        private String serviceURL;
        private Bitmap bitmap;
        private int reqWidth;
        private int reqHeight;

        private MyAsyncTask (ImageLoaderFragment fragment, String serviceURL) {
            this.fragmentWeakRef = new WeakReference<ImageLoaderFragment>(fragment);
            this.serviceURL = serviceURL;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            this.reqWidth = params[0];
            this.reqHeight = params[1];

            try {
                this.bitmap = this.downloadBitmap(serviceURL);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (this.fragmentWeakRef.get() != null) {
                ImageLoaderFragment fragment = this.fragmentWeakRef.get();
                if (fragment != null) {
                    fragment.mCallbackActivity = (OnImageLoadListener) fragment.getActivity();
                    fragment.mCallbackActivity.showBitmap(this.bitmap);
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

        private Bitmap downloadBitmap(String url) throws IOException {
            Log.v("DEBUG","Downloading " + url);
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

                publishProgress((int)((offset*100) / contentSize));
            }

            // First decode with inJustDecodeBounds=true to check original dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            // Calculate inSampleSize for down sampling size
            options.inSampleSize = calculateInSampleSize(options, this.reqWidth, this.reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            Log.i("DEBUG", "Final size " + options.outWidth + ", " + options.outHeight);
            Log.i("DEBUG", "Final bytes " + (bitmap.getRowBytes() * bitmap.getHeight()));

            // Save bitmap to memory cache
            ImageLoaderFragment fragment = this.fragmentWeakRef.get();
            if (fragment != null) {
                fragment.addBitmapToMemoryCache(ImageKey, bitmap);
            }

            return bitmap;
        }

        private  int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }
}
