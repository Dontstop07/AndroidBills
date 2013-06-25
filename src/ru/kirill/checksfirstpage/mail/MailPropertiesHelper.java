package ru.kirill.checksfirstpage.mail;

import android.content.SharedPreferences;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by oleg on 17.06.13.
 */
public class MailPropertiesHelper {

    private static final String SMTP_ADDRESS = "smtpAddress";
    private static final String SMTP_PORT = "smtpPort";

    public static final String SMTP_USER = "smtpUser";
    public static final String SMTP_PASSWORD = "smtpPassword";

    private static final String SMTP_SENDER = "smtpSender";
    private static final String SMTP_TO = "smtpTo";

    public static final String POP3_ADDRESS = "pop3Address";
    public static final String POP3_PORT = "pop3Port";
    public static final String USE_SMTP_LOGIN_AND_PASSWORD_IN_POP3 = "useSmtpLoginAndPasswordInPop3";
    public static final String POP3_USER = "pop3User";
    public static final String POP3_PASSWORD = "pop3Password";

    public static class StoredProperty {
        private static final String NOT_ASSIGNED = "NOT_ASSIGNED";
        public final String summary1;
        public final String summary2;
        private String value;
        private String key;
        private boolean assigned;
        public boolean showAssignedValue;

        public StoredProperty(String key, String summary1, String summary2, boolean showAssignedValue) {
            this.key = key;
            this.summary1 = summary1;
            this.summary2 = summary2;
            this.showAssignedValue = showAssignedValue;
        }

        public StoredProperty(String key, String summary1, String summary2) {
            this(key, summary1, summary2, true); // вызываем конструктор с расширенными параметрами
        }

        public StoredProperty(String key, String summary1, boolean showAssignedValue) {
            this(key, summary1, null, showAssignedValue); // вызываем конструктор с расширенными параметрами
        }

        public String getKey() {
            return key;
        }

        public String getValueAsString(SharedPreferences prefs) {
            return prefs.getString(key, NOT_ASSIGNED);
        }

        public boolean isAssigned(SharedPreferences prefs) {
            return getValueAsString(prefs).equals(NOT_ASSIGNED);
        }


    }

    public static List<StoredProperty> getProperties() {
        List<StoredProperty> result = new LinkedList<StoredProperty>();
        result.add(new StoredProperty(SMTP_ADDRESS, "Адрес smtp сервера.", "Например: smtp.gmail.com"));
        result.add(new StoredProperty(SMTP_PORT, "Номер порта smtp сервера.", "Например: 465"));
        result.add(new StoredProperty(SMTP_USER, "идентификатор пользователя для регистрации на smtp сервере.", "Например: userName@gmail.com"));
        result.add(new StoredProperty(SMTP_PASSWORD, "пароль для регистрации на smtp сервере.", false));
        result.add(new StoredProperty(SMTP_SENDER, "E-mail адрес отправителя.", "Например: userName@gmail.com"));
        result.add(new StoredProperty(SMTP_TO, "E-mail адрес получателя.", "Например: userName@gmail.com"));
        result.add(new StoredProperty(POP3_ADDRESS, "Адрес pop3 сервера.", "Например: pop.gmail.com"));
        result.add(new StoredProperty(POP3_PORT, "Номер порта pop3 сервера (SSL/TLS).", "Например: 995"));
        result.add(new StoredProperty(POP3_USER, "идентификатор пользователя для регистрации на pop3 сервере.", "Например: userName@gmail.com"));
        result.add(new StoredProperty(POP3_PASSWORD, "пароль для регистрации на pop3 сервере.", false));
        return result;
    }
}
