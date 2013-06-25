package ru.kirill.checksfirstpage.mail;

/**
 * Created by oleg on 06.06.13.
 */
public class BillsFilterImpl implements BillsFilter {
    @Override
    public boolean isBillsSubject(String subject) {
        return MailPartsCreator.getMessageSubject().equals(subject);
    }
}
