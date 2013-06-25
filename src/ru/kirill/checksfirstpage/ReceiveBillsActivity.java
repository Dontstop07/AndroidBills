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
import ru.kirill.checksfirstpage.mail.BillsFilter;
import ru.kirill.checksfirstpage.mail.BillsFilterImpl;
import ru.kirill.checksfirstpage.mail.receive.MailReceiveProperties;
import ru.kirill.checksfirstpage.mail.receive.MailReceivePropertiesImpl;
import ru.kirill.checksfirstpage.mail.receive.MailReceiver;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
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
        final MailReceiveProperties mailProperties = MailReceivePropertiesImpl.getInstance(this.getBaseContext());

        if (mailProperties.isPop3NotFilled()) {
            Toast.makeText(this, "Заполните параметры \"Pop3 сервер\" в настройках",
                           Toast.LENGTH_SHORT).show();
            return;
        }

        setLogText("");

        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                addLogText(msg.getData().getString("msg"));
            }
        };

        final MailReceiver mailReceiver = new MailReceiver(this);
        mailReceiver.setHandler(h);


        final Runnable rMailReceiver = new Runnable() {
            @Override
            public void run() {
                sendStringForLog("Начало приёма чеков.", h);
                try {
                    mailReceiver.receive();
                } catch (Exception e) {
                    sendStringForLog("Ошибка. Чеки не приняты.", h);
                    sendStringForLog(e.toString(), h);
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
        tvLogLines.setText(s + "\n" + tvLogLines.getText() );
    }
}
