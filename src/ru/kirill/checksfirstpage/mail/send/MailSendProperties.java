package ru.kirill.checksfirstpage.mail.send;

/**
 * Created by oleg on 01.06.13.
 */
public interface MailSendProperties {
    String getSMTPServerAddress();
    int getSMTPport();
    String getLoginName();
    String getLoginPassword();

    boolean isNotFilled();

    String getSender();

    String getRecipient();
}
