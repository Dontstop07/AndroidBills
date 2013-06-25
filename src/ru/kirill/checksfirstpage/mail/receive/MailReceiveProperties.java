package ru.kirill.checksfirstpage.mail.receive;

/**
 * Created by oleg on 01.06.13.
 */
public interface MailReceiveProperties {
    String getPop3ServerAddress();
    int getPop3Port();
    String getPop3LoginName();
    String getPop3LoginPassword();
    String getPop3Folder();

    boolean isPop3NotFilled();
}
