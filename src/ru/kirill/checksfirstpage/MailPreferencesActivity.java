package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by oleg on 03.06.13.
 */
public class MailPreferencesActivity extends Activity implements OnClickListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_mail_preferences);
    }

    @Override
    public void onClick(View v) {
        if(v == findViewById(R.id.btnSave)) {
//            getPreferences(Prefe)
        }
    }
}