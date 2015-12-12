package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.List;

public class CommonFreeTimePeriodsSearchFriendToAddFragment extends ListFragment
{
    public interface CommonFreeTimePeriodsSearchFriendToAddFragmentListener
    {
        void onCommonFreeTimePeriodsSearchFriendToAddFragmentNewFriendSelected(User user);
    }

    private List<User> filteredFriends = System.getInstance().getAppUser().getFriends();

    private CommonFreeTimePeriodsSearchFriendToAddFragmentListener listener;
    private CommonFreeTimePeriodsFriendsSearchResultsArrayAdapter selectedFriendsArrayAdapter;

    public CommonFreeTimePeriodsSearchFriendToAddFragment()
    {
    }

    public void setListener(CommonFreeTimePeriodsSearchFriendToAddFragmentListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        selectedFriendsArrayAdapter = new CommonFreeTimePeriodsFriendsSearchResultsArrayAdapter(getActivity(), 0, filteredFriends);
        setListAdapter(selectedFriendsArrayAdapter);
    }
    
    public void filterContentForSearchText (final String searchText)
    {
        if (searchText.equals(""))
        {
            filteredFriends = System.getInstance().getAppUser().getFriends();
        }
        else
        {
            Predicate<User> filterPredicate = new Predicate<User>()
            {
                @Override
                public boolean apply(User user)
                {
                    return user.getName().contains(searchText);
                }
            };

            filteredFriends = Lists.newArrayList(Collections2.filter(System.getInstance().getAppUser().getFriends(), filterPredicate));
        }

        selectedFriendsArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        if (listener != null)
        {
            listener.onCommonFreeTimePeriodsSearchFriendToAddFragmentNewFriendSelected(filteredFriends.get(position));
        }
    }

    public class CommonFreeTimePeriodsFriendsSearchResultsArrayAdapter extends ArrayAdapter<User>
    {
        Context context;
        List<User> objects;

        public CommonFreeTimePeriodsFriendsSearchResultsArrayAdapter(Context context, int resource, List<User> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_common_free_time_periods_search_friend_to_add_results, null);

            TextView button = (TextView) view.findViewById(R.id.nameTextView);
            button.setText(objects.get(position).getName());

            return view;
        }
    }
}
