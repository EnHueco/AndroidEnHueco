package com.diegoalejogm.enhueco.view;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.model.mainClasses.System;
import com.diegoalejogm.enhueco.model.mainClasses.User;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.Utilities;
import com.diegoalejogm.enhueco.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyProfileFragment extends Fragment
{
    private ImageView imageImageView;
    private ImageView backgroundImageView;

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

        TextView firstNamesTextView = (TextView) view.findViewById(R.id.firstNamesTextView);
        firstNamesTextView.setText(user.getFirstNames());

        TextView lastNamesTextView = (TextView) view.findViewById(R.id.lastNamesTextView);
        lastNamesTextView.setText(user.getLastNames());

        imageImageView = (ImageView) view.findViewById(R.id.imageImageView);
        backgroundImageView = (ImageView) view.findViewById(R.id.backgroundImageImageView);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateProfileImage();
    }

    public void updateProfileImage ()
    {
        User user = System.getInstance().getAppUser();

        if (user.getImageURL().isPresent() && !user.getImageURL().get().isEmpty())
        {
            Picasso.with(getContext()).load(EHURLS.BASE + user.getImageURL().get()).into(imageImageView);
            Picasso.with(getContext()).load(EHURLS.BASE + user.getImageURL().get()).into(backgroundImageView, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    backgroundImageView.setImageBitmap(Utilities.fastblur(((BitmapDrawable) backgroundImageView.getDrawable()).getBitmap(), 1, 120));

                    if (((MainTabbedActivity) getActivity()).getTabLayout().getSelectedTabPosition() == 2)  //This fragment is visible (TODO: Find a more elegant way to do this)
                    {
                        final AppBarLayout appBarLayout = ((MainTabbedActivity) getActivity()).getAppBarLayout();

                        Integer colorFrom = ((ColorDrawable)appBarLayout.getBackground()).getColor();
                        Integer colorTo = Color.argb(50, 20, 20, 20);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(400);

                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator)
                            {
                                appBarLayout.setBackgroundColor((Integer)animator.getAnimatedValue());
                            }
                        });
                        colorAnimation.start();
                    }
                }

                @Override
                public void onError()
                {
                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            if (imageImageView != null && imageImageView.getDrawable() != null)
            {
                final AppBarLayout appBarLayout = ((MainTabbedActivity) getActivity()).getAppBarLayout();

                Integer colorFrom = ((ColorDrawable)appBarLayout.getBackground()).getColor();
                Integer colorTo = Color.argb(50, 20, 20, 20);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(500);

                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator)
                    {
                        appBarLayout.setBackgroundColor((Integer)animator.getAnimatedValue());
                    }
                });
                colorAnimation.start();
            }

            System.getInstance().getAppUser().fetchUpdatesForAppUserAndSchedule();
        }
    }
}
