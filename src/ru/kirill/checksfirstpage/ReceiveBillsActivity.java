package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.textservice.SpellCheckerService;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import ru.kirill.checksfirstpage.mail.MailPartsCreator;
import ru.kirill.checksfirstpage.mail.send.Mail;
import ru.kirill.checksfirstpage.mail.send.MailProperties;
import ru.kirill.checksfirstpage.mail.send.MailSendProperties;

import javax.mail.*;
import java.util.Properties;

/**
 * Created by oleg on 01.06.13.
 */
public class ReceiveBillsActivity extends Activity implements View.OnClickListener {
    TextView tvLogLines;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_receive_bills);
        Button btnReceive = (Button) findViewById(R.id.btnReceive);
        btnReceive.setOnClickListener(this);
        Button btnMailPreferences = (Button) findViewById(R.id.btnMailPreferences);
        btnMailPreferences.setOnClickListener(this);
        tvLogLines = (TextView) findViewById(R.id.tvLogLines);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReceive: {
                // кнопка  Отправить чеки на почту
                receiveMail();
                break;
            }
            case R.id.btnMailPreferences: {
                // кнопка Настройки
//                Intent intent = new Intent(this, MailPreferencesActivity.class);
                Intent intent = new Intent(this, MailPreferencesActivity2.class);
                startActivity(intent);
                break;
            }
        }
    }

    private void receiveMail() {
        final MailSendProperties mailProperties = MailProperties.getInstance(this.getBaseContext());

        if (mailProperties.isNotFilled()) {
            Toast.makeText(this, "Заполните параметры в настройках",
                           Toast.LENGTH_SHORT).show();
            return;
        }

//        final Mail m = new Mail(mailProperties.getLoginName(),
//                                mailProperties.getLoginPassword());
//        m.setPort(""+mailProperties.getSMTPport());
//        m.setHost(mailProperties.getSMTPServerAddress());
//
//        m.setTo(new String[]{mailProperties.getRecipient()});
//        m.setFrom(mailProperties.getSender());
//        m.setSubject(MailPartsCreator.getMessageSubject());
//        m.setBody(MailPartsCreator.getMessageBody(this).toString());

        setLogText("");

        final Handler h = new Handler() {
            int cnt = 0;
            @Override
            public void handleMessage(Message msg) {
                addLogText(""+(cnt++) + ". "+  msg.getData().getString("msg"));
            }
        };

        final Runnable rMailReceiver = new Runnable() {
            @Override
            public void run() {
                sendStringForLog("Начало приёма чеков.", h);
                Properties pop3props = new Properties();
                pop3props.setProperty("mail.pop3.socketFactory.class",
                                      "javax.net.ssl.SSLSocketFactory");
                pop3props.setProperty("mail.pop3.socketFactory.fallback", "false");
                pop3props.setProperty("mail.pop3.port", "995");

                URLName url = new URLName("pop3", "pop.gmail.com", 995, "",
                        "olegch70@gmail.com",
                        "DimaLarisa3");

                Session session = Session.getInstance(pop3props, null);
                Exception ex=null;
                try {
                    Store store = session.getStore(url);
                    store.connect();
                    Folder folder = store.getFolder("inbox");
                    folder.open(Folder.READ_ONLY);
                    javax.mail.Message[] messages = folder.getMessages();
                    int i = 0;
                    for(javax.mail.Message mesage: messages) {
                        sendStringForLog(""+i + " " + mesage.getSubject(), h);
                        i++;
                    }
                    sendStringForLog("Чеки приняты успешно.", h);
                    folder.close(false); // Не удаляем удалённые сообщения
                    store.close();

                } catch (NoSuchProviderException e) {
                    ex=e;
                } catch (MessagingException e) {
                    ex=e;
                }

                if(ex != null) {
                    sendStringForLog("Ошибка. Чеки не ниняты.", h);
                    sendStringForLog(ex.toString(), h);
                }
            }
        };
        new Thread(rMailReceiver).start();
    }

    private void sendStringForLog(String data, Handler h) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", (String) data);
        msg.setData(bundle);
        h.sendMessage(msg);
    }

    private void setLogText(StringBuilder sb) {
        setLogText(sb.toString());
    }
    private void setLogText(String sb) {
        tvLogLines.setText(sb.toString());
    }

    private void addLogText(String s) {
        tvLogLines.setText(tvLogLines.getText() + "\n" + s);
    }
}
