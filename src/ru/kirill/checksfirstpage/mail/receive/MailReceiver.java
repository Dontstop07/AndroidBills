package ru.kirill.checksfirstpage.mail.receive;

import android.content.Context;
import android.os.*;
import com.sun.mail.pop3.POP3Folder;
import ru.kirill.checksfirstpage.db.Db;
import ru.kirill.checksfirstpage.dto.BillDto;
import ru.kirill.checksfirstpage.mail.BillsFilter;
import ru.kirill.checksfirstpage.mail.BillsFilterImpl;
import ru.kirill.checksfirstpage.mail.MailPartsParser;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Created by oleg on 07.06.13.
 */
public class MailReceiver {
    Context context;
    private int handledMailCount;
    public static final String CHARSET_NAME_UTF_8 = "UTF-8";
    public static final String CHARSET_NAME_WIN_1251 = "windows-1251";
    private List<BillDto> bills;

    public MailReceiver(Context ctx) {
        context = ctx;
    }

    private Handler h;

    public void setHandler(Handler h) {
        this.h = h;
    }

    public List<BillDto> receive() throws MessagingException {
        final BillsFilter filter = new BillsFilterImpl();

        MailReceiveProperties mailProperties = MailReceivePropertiesImpl.getInstance(context);
        Properties pop3props = new Properties();
        pop3props.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        pop3props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3props.setProperty("mail.pop3.port", ""+mailProperties.getPop3Port());

        URLName url = new URLName("pop3", mailProperties.getPop3ServerAddress(),
                mailProperties.getPop3Port(), "",
                mailProperties.getPop3LoginName(),
                mailProperties.getPop3LoginPassword());

        Session session = Session.getInstance(pop3props, null);
        Store store = session.getStore(url);
        store.connect();
        POP3Folder folder = (POP3Folder) store.getFolder(mailProperties.getPop3Folder());
        folder.open(Folder.READ_ONLY);
        javax.mail.Message[] messages = folder.getMessages();
        handledMailCount = 0;
        for(javax.mail.Message message: messages) {
            String uuid = folder.getUID(message);
            sendStringForLog("== почтовое сообщение == " + uuid);
            String subject = message.getSubject();
            if( filter.isBillsSubject(subject)) {
                handledMailCount++;
                sendStringForLog("" + handledMailCount + ". " + subject);
                handleMail(message);

            }
        }
        sendStringForLog("Чеки приняты успешно.");
        folder.close(false); // Не удаляем удалённые сообщения
        store.close();
        return bills;
    }

    private void handleMail(Message message) throws MessagingException {
        InputStreamReader isr = null;

        try {
//            sendStringForLog(message.getContent().getClass().toString());
            DataHandler dataHandler = message.getDataHandler();
            String contentType = dataHandler.getContentType();
//            sendStringForLog(contentType);
            if("text/plain".equals(contentType)) {
                InputStream is = dataHandler.getInputStream();
                isr = new InputStreamReader(is, CHARSET_NAME_WIN_1251);
            } else if(contentType != null && contentType.startsWith("multipart/mixed")) {
//                sendStringForLog(dataHandler.getContent().getClass().toString());
                MimeMultipart part = (MimeMultipart) dataHandler.getContent();
//                sendStringForLog("parts count "+part.getCount());
                for (int ii = 0; ii < part.getCount(); ii++) {
//                    sendStringForLog("parts count " + part.getCount());
                    BodyPart bodyPart = part.getBodyPart(ii);
                    Enumeration enumeration = bodyPart.getAllHeaders();
//                    sendStringForLog("headers");
                    String charSetName = "UTF-8";
                    while(enumeration.hasMoreElements()) {
                        Header elem = (Header) enumeration.nextElement();
//                        sendStringForLog(elem.getName()+ " => " + elem.getValue());
                        if("Content-Type".equals(elem.getName())) {
                            // "text/plain;"
                            String charsetPrefix = "charset=";
                            int _pos = elem.getValue().indexOf(charsetPrefix);
                            if(_pos > -1) {
                                charSetName = elem.getValue().substring(_pos+ charsetPrefix.length());

                                if(charSetName.startsWith(CHARSET_NAME_UTF_8)) {
                                    charSetName = CHARSET_NAME_UTF_8;
                                } else if(charSetName.startsWith(CHARSET_NAME_WIN_1251)) {
                                    charSetName = CHARSET_NAME_WIN_1251;
                                }
                                break;
                            }
                        }
                    }
                    InputStream is = bodyPart.getInputStream();
                    isr = new InputStreamReader(is, charSetName);
                }
            }

            if(isr != null) {
                BufferedReader br = new BufferedReader(isr);
                MailPartsParser mailPartsParser = new MailPartsParser();
                while(true) {
                    String line = br.readLine();
                    try {
                        if(line == null) {
                            bills = mailPartsParser.getBills();
                            break;
                        } else if ( ! line.trim().equals("")) {
                            mailPartsParser.parseBodyLine(line);
                        }
                    } catch (Exception e) {
                        sendStringForLog(line);
                        sendStringForLog(e.toString());
                    }
                }
                isr.close();

                importParsedBills(bills);
            }
        } catch (IOException e) {
            sendStringForLog(e.toString());
        }
    }

    private void importParsedBills(List<BillDto> bills) {
        Db db = new Db(context);
        db.open();
        try {
            for (BillDto billDtoImported: bills) {
                BillDto billDtoByUuid = db.getBillDtoByUuid(billDtoImported.uuid);
                if(billDtoByUuid == null) {
                    db.insert(billDtoImported);
                } else {
                    if(billDtoImported.inputDate.compareTo(billDtoByUuid.inputDate) > 0) {
                        // чек загруженный из почтового сообщения, новее (изменён позже), чем существующий чек
                        // заменим в БД существующий чек загруженным
                        // для этого достаточно в загруженном чеке указать идентификатор (поле _id) записи в БД
                        billDtoImported.id = billDtoByUuid.id;
                        db.edit(billDtoImported, true);
                    }
                }
//                sendStringForLog(MailPartsCreator.serializeBillDto(billDto));
            }
        } finally {
            db.close();
        }
    }

    private void sendStringForLog(String s) {
        android.os.Message msg = new android.os.Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", s);
        msg.setData(bundle);
        h.sendMessage(msg);
    }
}
