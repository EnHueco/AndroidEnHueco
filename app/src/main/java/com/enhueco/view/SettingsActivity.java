package com.enhueco.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AppUserInformationManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacyManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacyPolicy;
import com.enhueco.model.logicManagers.privacyManager.PrivacySetting;
import com.enhueco.model.model.intents.AppUserIntent;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.Utilities;
import com.enhueco.view.dialog.EHProgressDialog;
import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        /*
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        */

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key)
    {
        final EHProgressDialog dialog = new EHProgressDialog(SettingsActivity.this);
        dialog.show();

        if(key.equals(PrivacyManager.phoneNumberKey))
        {
            final String number = sharedPreferences.getString(key,"");
            AppUserIntent intent = new AppUserIntent();
            intent.setPhoneNumber(number);
            AppUserInformationManager.getSharedManager().updateAppUser(intent, new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception error)
                {
                    dialog.dismiss();
                    Utilities.showErrorToast(SettingsActivity.this);
                }
            });
        }
        else if(key.equals(PrivacyManager.sharesEventsLocationKey) || key.equals(PrivacyManager.sharesEventsNameKey))
        {

            final Boolean on = sharedPreferences.getBoolean(key,false);
            final PrivacySetting setting = key.equals(PrivacyManager.sharesEventsLocationKey) ? PrivacySetting.SHOW_EVENT_LOCATIONS : PrivacySetting.SHOW_EVENT_NAMES;
            PrivacyManager.getSharedManager().turnSetting(on,setting, Optional.<PrivacyPolicy>absent(), new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    dialog.dismiss();
                    Log.v("SettingsActivity", "Changed preference: " + setting.toString() + " to " + on);
                }

                @Override
                public void onFailure(Exception error)
                {
                    dialog.dismiss();
                    Utilities.showErrorToast(getApplicationContext());
                    error.printStackTrace();
                    sharedPreferences.edit().putBoolean(key, !on);
                }
            });
        }

        Log.v("Settings Activity", "Preference changed: " + key);
    }


    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }
}
