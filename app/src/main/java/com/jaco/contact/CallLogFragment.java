package com.jaco.contact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 7/28/16.
 */
public class CallLogFragment extends Fragment {

    public CallLogFragment() {}

    public static Fragment newInstance() {
        return new CallLogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_list, container, false);

        MyCallLog callLog = new MyCallLog(getActivity());
        new mAsyncTask(getActivity()).execute(callLog.getCallLog());

        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();

        //ocultar campo de busqueda
        final LinearLayout search_layout = (LinearLayout) getActivity().findViewById(R.id.search_layout);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_to_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                search_layout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (search_layout.getVisibility() != View.GONE)
            search_layout.startAnimation(animation);

    }

    public static class mAsyncTask extends AsyncTask<List<MyCallLog.Call>, Void, List<CallDetails>>{

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;

        public mAsyncTask(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {

            final Activity activity = weakReference.get();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = ProgressDialog.show(activity, "",
                            activity.getString(R.string.getting), true);
                }
            });

        }

        @Override
        protected List<CallDetails> doInBackground(List<MyCallLog.Call>... lists) {

            List<CallDetails> callDetail = new ArrayList<>();

            for (MyCallLog.Call call : lists[0]) {
                callDetail.add(new CallDetails(weakReference.get(), call));
            }

            return callDetail;
        }

        @Override
        protected void onPostExecute(List<CallDetails> callDetail) {

            RecyclerView recyclerView = (RecyclerView) weakReference.get().findViewById(R.id.recycler_view_list);
            CallLogAdapter adapter = (CallLogAdapter) recyclerView.getAdapter();
            boolean animate = false;
            if (adapter == null){
                adapter = new CallLogAdapter(new ArrayList<CallDetails>());
                animate = true;
            }
            adapter.addAll(callDetail);
            recyclerView.swapAdapter(adapter, false);

            if (animate)
                recyclerView.startAnimation(AnimationUtils.loadAnimation(weakReference.get(), R.anim.enter_from_bottom));

            progressDialog.dismiss();
        }
    }

}
