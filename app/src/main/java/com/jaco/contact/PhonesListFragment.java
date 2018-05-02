package com.jaco.contact;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhonesListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhonesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhonesListFragment extends Fragment {

    private static final String ARG_PHONE_ENTRY = "phone_entry";

    private PhoneEntry[] phoneEntry;

    private OnFragmentInteractionListener mListener;

    public PhonesListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PhonesListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment newInstance(PhoneEntry[] entries) {
        PhonesListFragment fragment = new PhonesListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHONE_ENTRY, entries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneEntry = (PhoneEntry[]) getArguments().getSerializable(ARG_PHONE_ENTRY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootview.findViewById(R.id.recycler_view_list);
        ContactsItemList adapter = new ContactsItemList(phoneEntry);
        recyclerView.setAdapter(adapter);

        return rootview;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnStartFragmentInteractionListener) {
//            mListener = (OnStartFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnStartFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
