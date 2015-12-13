package com.diegoalejogm.enhueco.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;
import com.squareup.picasso.Picasso;


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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        System.getInstance().getAppUser().fetchUpdatesForAppUserAndSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        User user = System.getInstance().getAppUser();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
        tv1.setText(user.getName());

        TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);
        tv2.setText(user.getUsername());

        ImageView profileImage= (ImageView) view.findViewById(R.id.profileImageImageView);

        if (user.getImageURL().isPresent() && !user.getImageURL().get().isEmpty())
        {
            Picasso.with(this.getContext()).load(user.getImageURL().get()).into(profileImage);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            System.getInstance().getAppUser().fetchUpdatesForAppUserAndSchedule();
        }
    }
}
