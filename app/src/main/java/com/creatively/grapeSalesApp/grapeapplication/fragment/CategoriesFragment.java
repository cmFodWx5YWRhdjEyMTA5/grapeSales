package com.creatively.grapeSalesApp.grapeapplication.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.creatively.grapeSalesApp.grapeapplication.R;

public class CategoriesFragment extends Fragment implements View.OnClickListener {
    private RelativeLayout layout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        init(view);
        return view;
    }


    public void init(View view) {
        TextView title = getActivity().findViewById(R.id.registerTitle);
        title.setText(getString(R.string.main));
        RelativeLayout clothLayout = view.findViewById(R.id.clothLayout);
        RelativeLayout shopsLayout = view.findViewById(R.id.shopsLayout);
        RelativeLayout hosusLayout = view.findViewById(R.id.hosusLayout);
        RelativeLayout sportLayout = view.findViewById(R.id.sportLayout);
        RelativeLayout furLayout = view.findViewById(R.id.furLayout);
        RelativeLayout resturanceLayout = view.findViewById(R.id.resturanceLayout);
        TextView select = view.findViewById(R.id.select);
        select.setText(getString(R.string.select_category_store));
        layout = view.findViewById(R.id.layout);
        clothLayout.setOnClickListener(this);
        shopsLayout.setOnClickListener(this);
        hosusLayout.setOnClickListener(this);
        sportLayout.setOnClickListener(this);
        furLayout.setOnClickListener(this);
        resturanceLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.clothLayout) {
            replaceFragment(new PlacesFragment(), "Fashion");
        } else if (id == R.id.shopsLayout) {
            replaceFragment(new PlacesFragment(), "Shops");
        } else if (id == R.id.hosusLayout) {
            replaceFragment(new PlacesFragment(), "Houseware");
        } else if (id == R.id.sportLayout) {
            replaceFragment(new PlacesFragment(), "sports tool");
        } else if (id == R.id.furLayout) {
            replaceFragment(new PlacesFragment(), "furniture");
        } else if (id == R.id.resturanceLayout) {
            replaceFragment(new PlacesFragment(), "Restaurants");
        }
    }

    public void replaceFragment(Fragment fragment, String data) {
        layout.setVisibility(View.GONE);
        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        Bundle arguments = new Bundle();
        arguments.putString("category", data);
        fragment.setArguments(arguments);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, fragment.toString());
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }
}
