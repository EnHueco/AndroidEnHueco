package com.diegoalejogm.enhueco.View;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyProfileFragment extends Fragment
{


    public MyProfileFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        TextView tv1 = (TextView) view.findViewById(R.id.fragmentMyProfile_firstNameTextView);
        tv1.setText(System.instance.getAppUser().getFirstNames());
        TextView tv2 = (TextView) view.findViewById(R.id.fragmentMyProfile_lastNameTextView);
        tv2.setText(System.instance.getAppUser().getLastNames());
        TextView tv3 = (TextView) view.findViewById(R.id.fragmentMyProfile_userNameTextView);
        tv3.setText(System.instance.getAppUser().getUsername());
        return view;
    }


}
