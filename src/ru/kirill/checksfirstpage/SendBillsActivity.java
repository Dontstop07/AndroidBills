package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import ru.kirill.checksfirstpage.db.Db;
import ru.kirill.checksfirstpage.mail.MailPartsCreator;
import ru.kirill.checksfirstpage.mail.send.Mail;
import ru.kirill.checksfirstpage.mail.send.MailProperties;
import ru.kirill.checksfirstpage.mail.send.MailSendProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by oleg on 01.06.13.
 */
public class SendBillsActivity extends Activity implements View.OnClickListener {
    TextView tvLogLines;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_send_bills);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        Button btnMailPreferences = (Button) findViewById(R.id.btnMailPreferences);
        btnMailPreferences.setOnClickListener(this);
        tvLogLines = (TextView) findViewById(R.id.tvLogLines);
        setLogLinesAsSubjAndText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend: {
                // кнопка  Отправить новые чеки на почту
                sendMail(false);
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

    public void btnSendAllOnClick(View btn) {
        // кнопка  Отправить новые чеки на почту
        sendMail(true);
    }

    private void sendMail(final boolean sendAll) {
        final MailSendProperties mailProperties = MailProperties.getInstance(this.getBaseContext());

        if (mailProperties.isNotFilled()) {
            Toast.makeText(this, "Заполните параметры в настройках",
                           Toast.LENGTH_SHORT).show();
            return;
        }

        setLogText("");

        final Handler h = new Handler() {
            int cnt = 0;
            @Override
            public void handleMessage(Message msg) {
                addLogText(""+(cnt++) + ". "+  msg.getData().getString("msg"));
            }
        };

        final Runnable rMailSender = new Runnable() {
            @Override
            public void run() {
                long[] ids;
                String messageForZeroMessages;
                if(sendAll) {
                    ids = MailPartsCreator.getNotImportedAndEditedBillsIds(SendBillsActivity.this);
                    messageForZeroMessages = "Чеки отсутствуют.";
                } else {
                    ids = MailPartsCreator.getNewBillsIds(SendBillsActivity.this);
                    messageForZeroMessages = "Новых чеков нет.";
                }

                if(ids.length == 0) {
                    sendStringForLog(messageForZeroMessages, h);
                    return;
                }

                Mail m = new Mail(mailProperties.getLoginName(),
                        mailProperties.getLoginPassword());
                m.setPort(""+mailProperties.getSMTPport());
                m.setHost(mailProperties.getSMTPServerAddress());

                m.setTo(new String[] {mailProperties.getRecipient()});
                m.setFrom(mailProperties.getSender());
                m.setSubject(MailPartsCreator.getMessageSubject());

                Db db = new Db(SendBillsActivity.this);
                db.open();
                db.beginTransaction();
                m.setBody(MailPartsCreator.getMessageBody(SendBillsActivity.this, db, ids).toString());

                try {
                    sendStringForLog("Начало отправки чеков.", h);
                    m.send();
                    sendStringForLog("Чеки отправлены успешно.", h);
                    for(long id: ids) {
                        // Установим для чеков признак "отправлен"
                        db.setSended(id);
                    }
                    db.transactionSuccessful();
                } catch (Exception e) {
                    sendStringForLog("Ошибка. Чеки не отправлены.", h);
                    sendStringForLog(e.toString(), h);
                }
                db.endTransaction();
                db.close();
            }
        };
        new Thread(rMailSender).start();
    }

    private void sendStringForLog(String data, Handler h) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", (String) data);
        msg.setData(bundle);
        h.sendMessage(msg);
    }

    private void setLogLinesAsSubjAndText() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Subj: ").append(MailPartsCreator.getMessageSubject()).append("\n");
//        sb.append(MailPartsCreator.getMessageBody(this));
//        setLogText(sb);
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
