package ru.kirill.checksfirstpage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import ru.kirill.checksfirstpage.mail.MailPropertiesHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oleg on 03.06.13.
 */
public class MailPreferencesActivity2 extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Map<String,MailPropertiesHelper.StoredProperty> summaryValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smtp_preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        summaryValues = new HashMap<String, MailPropertiesHelper.StoredProperty>();
        PreferenceScreen scr = getPreferenceScreen();
        List<MailPropertiesHelper.StoredProperty> properties = MailPropertiesHelper.getProperties();
        PreferenceManager preferenceManager = scr.getPreferenceManager();
        SharedPreferences sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(this);
        for(MailPropertiesHelper.StoredProperty property: properties) {
            Preference preference = preferenceManager.findPreference(property.getKey());
            if(preference == null) {
                continue;
            }
            summaryValues.put(property.getKey(), property);

            setSummary(sharedPreferences, property, preference);
        }
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    private void setSummary(SharedPreferences sharedPreferences, MailPropertiesHelper.StoredProperty property, Preference preference) {
        String assignedValue = sharedPreferences.getString(property.getKey(), "---");
        if("---".equals(assignedValue)) {
            assignedValue = "";
        } else {
            if(property.showAssignedValue) {
                assignedValue = "\n"+assignedValue;
            } else {
                assignedValue = "\n**********";
            }
        }
        if(property.summary2 != null && assignedValue.trim().length() == 0 ) {
            assignedValue += "\n"+property.summary2;
        }
        preference.setSummary(property.summary1+assignedValue);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        summaryValues = null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
        MailPropertiesHelper.StoredProperty property = summaryValues.get(key);
        if(property != null) {
            Preference preference = findPreference(key);
            setSummary(sharedPreferences, property, preference);
        }

    }
}
