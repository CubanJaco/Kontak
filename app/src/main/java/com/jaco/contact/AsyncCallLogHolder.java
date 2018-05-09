package com.jaco.contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.jaco.contact.CallLogAdapter.mHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

/**
 * Created by osvel on 5/8/18.
 */

public class AsyncCallLogHolder {

    private final WeakReference<CallLogWorkerTask> callLogWorkerTask;

    private AsyncCallLogHolder(CallLogWorkerTask bitmapWorkerTaskReference) {
        this.callLogWorkerTask = new WeakReference<>(bitmapWorkerTaskReference);
    }

    public CallLogWorkerTask getCallLogWorkerTask() {
        return callLogWorkerTask.get();
    }

    private static CallLogWorkerTask getCallLogWorkerTask(mHolder holder) {
        if (holder != null) {
            final AsyncCallLogHolder async = holder.getAsyncCallLog();
            return async != null ? async.getCallLogWorkerTask() : null;
        }
        return null;
    }

    public static void loadCallLog(Context context, MyCallLog.Call call, mHolder holder) {
        if (cancelPotentialWork(call, holder)) {
            final CallLogWorkerTask task = new CallLogWorkerTask(context, holder, call);
            final AsyncCallLogHolder asyncCallLog =
                    new AsyncCallLogHolder(task);
            holder.setAsyncCallLog(asyncCallLog);
            task.execute();
        }
    }

    private static boolean cancelPotentialWork(MyCallLog.Call call, mHolder holder) {
        final CallLogWorkerTask workerTask = getCallLogWorkerTask(holder);

        if (workerTask != null) {
            final MyCallLog.Call data = workerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (data == null || !data.equals(call)) {
                // Cancel previous task
                workerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static Callback getPicassoCallback(final Context context, final ImageView imageView, final int dim){

        return new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(context)
                        .load(R.drawable.user)
                        .resize(dim, dim)
                        .transform(MainActivity.transformation)
                        .into(imageView);
            }
        };

    }

    private static class CallLogWorkerTask extends AsyncTask<MyCallLog.Call, Void, CallDetails> {

        private WeakReference<Context> weakReference;
        private WeakReference<mHolder> weakHolder;
        private MyCallLog.Call data;
        private int dim;
        private Drawable placeHolder;

        public CallLogWorkerTask(Context context, mHolder weakHolder, MyCallLog.Call call) {
            this.weakReference = new WeakReference<>(context);
            this.weakHolder = new WeakReference<>(weakHolder);
            this.data = call;
            dim = (int) context.getResources().getDimension(R.dimen.icon_size);
        }

        @Override
        protected CallDetails doInBackground(MyCallLog.Call... params) {
            if (placeHolder == null){
                Bitmap bmp = new CircleTransform().transform(
                        BitmapFactory.decodeResource(weakReference.get().getResources(),
                                R.drawable.user));
                placeHolder = new BitmapDrawable(weakReference.get().getResources(), bmp);
            }

            return new CallDetails(weakReference.get(), data);
        }

        @Override
        protected void onPostExecute(CallDetails call) {
            super.onPostExecute(call);

            //llenar el holder con los datos reales
            if (weakHolder == null || weakReference == null)
                return;

            weakHolder.get().contact_name.setText(call.getName());

            Uri displayPhotoUri = call.getImageUri();
            Picasso.with(weakReference.get())
                    .load(displayPhotoUri)
                    .resize(dim, dim)
                    .transform(MainActivity.transformation)
                    .placeholder(placeHolder)
                    .into(weakHolder.get().contact_image,
                            getPicassoCallback(weakReference.get(), weakHolder.get().contact_image, dim));

        }
    }

}
