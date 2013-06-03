package ru.kirill.checksfirstpage.mail.send;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by oleg on 01.06.13.
 */
public class MailProperties {

    public static final String NOT_ASSIGNED = "notAssigned";
    public static final int NOT_ASSIGNED_INT = -1;

    public static MailSendProperties getInstance(final Context ctx) {
        return new MailSendProperties() {
            SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
            @Override
            public boolean isNotFilled() {
                return     NOT_ASSIGNED.equals(getSMTPServerAddress())
                        || NOT_ASSIGNED_INT == getSMTPport()
                        || NOT_ASSIGNED.equals(getLoginName())
                        || NOT_ASSIGNED.equals(getLoginPassword())
                        || NOT_ASSIGNED.equals(getSender())
                        || NOT_ASSIGNED.equals(getRecipient());
            }

            @Override
            public String getSMTPServerAddress() {
                return prefs.getString("smtpAddress", NOT_ASSIGNED);
            }

            @Override
            public int getSMTPport() {
                String sPortNumber = prefs.getString("smtpPort", ""+NOT_ASSIGNED_INT);
                int iPortNumber = NOT_ASSIGNED_INT;
                try {
                    iPortNumber = Integer.parseInt(sPortNumber);
                } catch (NumberFormatException ignore) {
                }
                return iPortNumber;
            }

            @Override
            public String getLoginName() {
                return prefs.getString("smtpUser", NOT_ASSIGNED);
            }

            @Override
            public String getLoginPassword() {
                return prefs.getString("smtpPassword", NOT_ASSIGNED);
            }

            @Override
            public String getSender() {
                return prefs.getString("smtpSender", NOT_ASSIGNED);
            }

            @Override
            public String getRecipient() {
                return prefs.getString("smtpTo", NOT_ASSIGNED);
            }
        };
    }
}
