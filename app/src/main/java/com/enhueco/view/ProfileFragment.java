package com.enhueco.view;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AppUserInformationManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment
{
    private ImageView imageImageView;
    private ImageView backgroundImageView;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Get extra data included in the Intent
            if (intent.getAction().equals(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE))
            {
                ArrayList<User> users = (ArrayList<User>) intent.getSerializableExtra(FriendRequestsActivity.EXTRA_REQUESTS);
                ProfileFragment.this.refresh();
            }

//            Log.d("receiver", "Got message: " + message);
        }
    };

    public ProfileFragment()
    {
        // Required empty public constructor
    }

    public void refresh()
    {
        // TODO: Update image
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        AppUserInformationManager.getSharedManager().fetchUpdatesForAppUserAndSchedule(new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                refresh();
            }

            @Override
            public void onFailure(Exception error)
            {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        User user = EnHueco.getInstance().getAppUser();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_my_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateProfileImage();
    }

    public void updateProfileImage ()
    {
        User user = EnHueco.getInstance().getAppUser();

        if (user.getImageURL().isPresent() && !user.getImageURL().get().isEmpty())
        {
            Picasso.with(getContext()).load(EHURLS.BASE + user.getImageURL().get()).into(imageImageView, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            backgroundImageView.setImageBitmap(Utilities.fastblur(((BitmapDrawable) imageImageView.getDrawable()).getBitmap(), 0.1f, 20));
                        }
                    });

                    if (((MainTabbedActivity) getActivity()).getTabLayout().getSelectedTabPosition() == 2)  //This fragment is visible (TODO: Find a more elegant way to do this)
                    {
                        final AppBarLayout appBarLayout = ((MainTabbedActivity) getActivity()).getAppBarLayout();
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
            /*
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
            */

            AppUserInformationManager.getSharedManager().fetchUpdatesForAppUserAndSchedule(new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    refresh();
                }

                @Override
                public void onFailure(Exception error)
                {

                }
            });
        }
    }


}
