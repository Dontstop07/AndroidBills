package ru.kirill.checksfirstpage.mail.receive;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ru.kirill.checksfirstpage.mail.MailPropertiesHelper;

/**
 * Created by oleg on 01.06.13.
 */
public class MailReceivePropertiesImpl {

    public static final String NOT_ASSIGNED = "notAssigned";
    public static final int NOT_ASSIGNED_INT = -1;

    public static MailReceiveProperties getInstance(final Context ctx) {
        return new MailReceiveProperties() {
            SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
            @Override
            public boolean isPop3NotFilled() {
                return     NOT_ASSIGNED.equals(getPop3ServerAddress())
                        || NOT_ASSIGNED_INT == getPop3Port()
                        || NOT_ASSIGNED.equals(getPop3LoginName())
                        || NOT_ASSIGNED.equals(getPop3LoginPassword());
            }

            @Override
            public String getPop3ServerAddress() {
                return prefs.getString(MailPropertiesHelper.POP3_ADDRESS, NOT_ASSIGNED);
            }

            @Override
            public String getPop3Folder() {
                return "INBOX";
            }

            @Override
            public int getPop3Port() {
                String sPortNumber = prefs.getString(MailPropertiesHelper.POP3_PORT, ""+NOT_ASSIGNED_INT);
                int iPortNumber = NOT_ASSIGNED_INT;
                try {
                    iPortNumber = Integer.parseInt(sPortNumber);
                } catch (NumberFormatException ignore) {
                }
                return iPortNumber;
            }

            @Override
            public String getPop3LoginName() {
                if(prefs.getBoolean(MailPropertiesHelper.USE_SMTP_LOGIN_AND_PASSWORD_IN_POP3, false)) {
                    return prefs.getString(MailPropertiesHelper.SMTP_USER, NOT_ASSIGNED);
                }
                return prefs.getString(MailPropertiesHelper.POP3_USER, NOT_ASSIGNED);
            }

            @Override
            public String getPop3LoginPassword() {
                if(prefs.getBoolean(MailPropertiesHelper.USE_SMTP_LOGIN_AND_PASSWORD_IN_POP3, false)) {
                    return prefs.getString(MailPropertiesHelper.SMTP_PASSWORD, NOT_ASSIGNED);
                }
                return prefs.getString(MailPropertiesHelper.POP3_PASSWORD, NOT_ASSIGNED);
            }
        };
    }

}
