package com.gnssapps.acq.torinex;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.GnssNavigationMessage;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import static com.gnssapps.acq.torinex.Constants.ACQ_REQUEST_ID;
import static com.gnssapps.acq.torinex.Constants.REQUIRED_PERMISSIONS;
import static com.gnssapps.acq.torinex.AppUtils.showInfoHelp;

/**
 * SettingsActivity, the main activity for this app, is a {@link PreferenceActivity} that
 * presents the menu with the list of application settings and activities that can be performed.
 * On handset devices, settings are presented as a single list.
 * On tablets,settings are split by category, with category headers shown to the left of the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private boolean appHasPermissions = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the {@link android.app.ActionBar}, if the API is available.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //state
        //check if OS version has the API level required
        if (Build.VERSION.SDK_INT < VERSION_CODES.N_MR1) {
            Toast.makeText(getApplicationContext(),getString(R.string.app_reqAPIlevel), Toast.LENGTH_LONG).show();
            this.finishAffinity();
        }
        //check and request permissions if needed
        appHasPermissions = true;
        for (String p : REQUIRED_PERMISSIONS)
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) appHasPermissions = false;
        if (!appHasPermissions) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, ACQ_REQUEST_ID);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == ACQ_REQUEST_ID) {
            // If request is cancelled, the result arrays are empty.
            appHasPermissions = true;
            for (int i=0; i<grantResults.length; i++)
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) appHasPermissions = false;
            if (!appHasPermissions) {
                Toast.makeText(getApplicationContext(), getString(R.string.app_perm_NOK), Toast.LENGTH_LONG).show();
                this.finishAffinity();
            }
            else Toast.makeText(getApplicationContext(),getString(R.string.app_perm_OK), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);//++frances
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_activity_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting_info:
                String currentTitle = getTitle().toString();
                if (currentTitle.equalsIgnoreCase(getString(R.string.app_name))) {
                    showInfoHelp(
                            getString(R.string.setting_activity_info_title),
                            getString(R.string.setting_activity_info_body),
                            this);
                } else if (currentTitle.equalsIgnoreCase(getString(R.string.pref_header_setupAcq))) {
                    showInfoHelp(
                            getString(R.string.setup_acquisition_info_title),
                            getString(R.string.setup_acquisition_info_body),
                            this);
                } else if (currentTitle.equalsIgnoreCase(getString(R.string.pref_header_setupRINEX))) {
                    showInfoHelp(
                            getString(R.string.setup_RINEX_info_title),
                            getString(R.string.setup_RINEX_info_body),
                            this);
                } else {
                    showInfoHelp("Current", currentTitle, this);
                }
                return true;
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Home selected", Toast.LENGTH_SHORT).show();
                //finish();
                //return true;
                //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*TBD
    @Override
    public boolean onIsMultiPane() {
        //to determine if the device has an extra-large screen. F.e. 10" tablets are extra-large.
        return (this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
    /**TBD
     * This method overrides the default implementation that returns true.
     * It stops fragment injection in malicious applications (?).
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SetupAcqPreferenceFragment.class.getName().equals(fragmentName)
                || SetupRINEXPreferenceFragment.class.getName().equals(fragmentName)
                || DataAcqPreferenceFragment.class.getName().equals(fragmentName)
                || GenerateRinexPreferenceFragment.class.getName().equals(fragmentName);
    }
   /**
     * sBindPreferenceSummaryToValueListener is a preference value change listener that updates
     * the preference's summary (line of text below the preference title) to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (preference instanceof RingtonePreference) {
             /*TBD
                // For ringtone preferences, look up the correct display value using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    //preference.setSummary(R.string.pref_ringtone_silent);
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
                */
            } else if (preference instanceof SwitchPreference) {
                //TBD
            } else {
                // For all other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
        return true;
        }
    };
    /**
     * bindPreferenceSummaryToValue binds a preference's summary to its value.
     * More specifically, when the preference's value is changed,its summary is updated to reflect the value.
     * The summary is also immediately updated upon calling this method.
     * The exact display format is dependent on the type of preference.
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        // Trigger the listener immediately with the preference's current value.
        SharedPreferences prefs = preference.getSharedPreferences();
        Object value;
        if (preference instanceof MultiSelectListPreference) {
            value = prefs.getStringSet(preference.getKey(), new HashSet<String>());
        } else {
            value = prefs.getString(preference.getKey(), Constants.EMPTY);
        }
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
    }
    /**
     * SetupAcqPreferenceFragment is linked in the preference-headers xml file to the setup acquisition option.
     * The preferences to be set are included in pref_setupacq.xml
     * It is used when the activity is showing a two-pane settings UI.(?)
     */
    public static class SetupAcqPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //set initial values
            Calendar calendar = Calendar.getInstance();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            String survey = String.format("S_%ty_%tm_%td_%tH_%tM",calendar,calendar,calendar,calendar,calendar);
            editor.putString("survey_name", survey);
            editor.commit();
            addPreferencesFromResource(R.xml.pref_setupacq);
            setHasOptionsMenu(true);
            // Bind the summaries of preferences to their values.
            // When their values change, their summaries are updated to reflect the new value.
            bindPreferenceSummaryToValue(findPreference("survey_name"));
            bindPreferenceSummaryToValue(findPreference("measurements_interval"));
            bindPreferenceSummaryToValue(findPreference("total_epochs"));
            //tbd
            //bindPreferenceSummaryToValue(findPreference("acquire_nav_message"));
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
    /**
     * DataAcqPreferenceFragment is linked in the preference-headers xml file to perform acquisition option.
     * This fragment starts the activity associated to PerformDataAcq, which acquires data from the GNSS receiver
     * and store them into files.
     */
    public static class DataAcqPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startActivity(new Intent(getActivity(), PerformDataAcq.class));
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    /**
     * SetupRINEXPreferenceFragment is linked in the preference-headers xml file to pref_header_setupRINEX option.
     * The preferences to be set are included in pref_setuprinex.xml, which are mostly related to the data values
     * to be included in the RINEX file header.
     */
    public static class SetupRINEXPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setuprinex);
            setHasOptionsMenu(true);
            // Bind the summaries of preferences to their values.
            // When their values change, their summaries are updated to reflect the new value.
            bindPreferenceSummaryToValue(findPreference("rinex_version"));
            bindPreferenceSummaryToValue(findPreference("rinex_site"));
            bindPreferenceSummaryToValue(findPreference("rinex_runby"));
            bindPreferenceSummaryToValue(findPreference("rinex_markername"));
            bindPreferenceSummaryToValue(findPreference("rinex_markertype"));
            bindPreferenceSummaryToValue(findPreference("rinex_observer"));
            bindPreferenceSummaryToValue(findPreference("rinex_agency"));
            bindPreferenceSummaryToValue(findPreference("rinex_receivernumber"));
            bindPreferenceSummaryToValue(findPreference("rinex_comment"));
            bindPreferenceSummaryToValue(findPreference("rinex_markernumber"));
            bindPreferenceSummaryToValue(findPreference("rinex_clkoffs"));
            //bindPreferenceSummaryToValue(findPreference("rinex_fitinterval"));
            bindPreferenceSummaryToValue(findPreference("rinex_loglevel"));
            bindPreferenceSummaryToValue(findPreference("rinex_constellations"));
            bindPreferenceSummaryToValue(findPreference("rinex_satellites"));
            bindPreferenceSummaryToValue(findPreference("rinex_observables"));
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    /**
     * GenerateRINEXPreferenceFragment is linked in the preference-headers xml file to perform RINEX files generation.
     * This fragment starts the activity associated to GenerateRINEX, which generate RINEX files from existing raw data
     * files previously acquired, and taking into account values and preferences stated in the setup RINEX preferences.
     */
    public static class GenerateRinexPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startActivity(new Intent(getActivity(), GenerateRinex.class));
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
