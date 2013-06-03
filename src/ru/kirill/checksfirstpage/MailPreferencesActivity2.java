package ru.kirill.checksfirstpage;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by oleg on 03.06.13.
 */
public class MailPreferencesActivity2 extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smtp_preferences);
    }
}
