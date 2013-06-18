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
import ru.kirill.checksfirstpage.dto.BillDto;
import ru.kirill.checksfirstpage.mail.MailPartsCreator;
import ru.kirill.checksfirstpage.mail.send.Mail;
import ru.kirill.checksfirstpage.mail.send.MailProperties;
import ru.kirill.checksfirstpage.mail.send.MailSendProperties;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    private void sendStringForLog(String data, Handler h) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", (String) data);
        msg.setData(bundle);
        h.sendMessage(msg);
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
                addLogText("" + (cnt++) + ". " + msg.getData().getString("msg"));
            }
        };

        final Runnable rMailSender = new Runnable() {
            @Override
            public void run() {
                long[] ids;
                String messageForZeroMessages;
                if (sendAll) {
                    ids = MailPartsCreator.getNotImportedAndEditedBillsIds(SendBillsActivity.this);
                    messageForZeroMessages = "Чеки отсутствуют.";
                } else {
                    ids = MailPartsCreator.getNewBillsIds(SendBillsActivity.this);
                    messageForZeroMessages = "Новых чеков нет.";
                }

                if (ids.length == 0) {
                    sendStringForLog(messageForZeroMessages, h);
                    return;
                }

                Mail m = new Mail(mailProperties.getLoginName(),
                        mailProperties.getLoginPassword());
                m.setPort("" + mailProperties.getSMTPport());
                m.setHost(mailProperties.getSMTPServerAddress());

                m.setTo(new String[]{mailProperties.getRecipient()});
                m.setFrom(mailProperties.getSender());
                m.setSubject(MailPartsCreator.getMessageSubject());

                sendStringForLog("Начало отправки чеков.", h);
                Db db = new Db(SendBillsActivity.this);
                db.open();
                db.beginTransaction();
                m.setBody(MailPartsCreator.getMessageBody(SendBillsActivity.this, db, ids).toString());

                try {
                    m.send();
                    sendStringForLog("Чеки отправлены успешно.", h);
                    for (long id : ids) {
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

/*
    public void btnSendBillsFromOldProgramOnClick(View btn) {
        // кнопка  Отправить чеки из старой программы на почту
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
                addLogText("" + (cnt++) + ". " + msg.getData().getString("msg"));
            }
        };

        final Runnable rMailSender = new Runnable() {
            @Override
            public void run() {
                String sBillFieldVlues = "";
                int currentYear = 0;
                int currentMonth = 0;
                List<BillDto> billsForMonth = new LinkedList<BillDto>();
                Calendar calendar = new GregorianCalendar();

                try {
                    Db db = new Db(SendBillsActivity.this);
                    db.open();

                    sendStringForLog("Начало отправки старых чеков.", h);

                    String[] expensesValues = getResources().getStringArray(R.array.expenses_values);

                    List<BillDto> checksForSend = new LinkedList<BillDto>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    for(String expensesFieldsValues: expensesValues) {
                        sBillFieldVlues = expensesFieldsValues;
                        // 500,2;развлечения;торт на окончание учебного года;23.05.2008;2008;5;1485;2407;;
                        // SUMMA;VID;DESCRIPTION;EXPDATE;EXPYEAR;EXPMONTH;EXPITEM;ID;DELETED;ITEMUUID
                        //  0    1   2           3       4       5        6       7  8       9
                        String value[] = expensesFieldsValues.split(";");
                        BillDto billDto = new BillDto();
                        billDto.cash = value[0].replace(",", ".");
                        billDto.kind = value[1].trim();
                        billDto.description = value[2];
                        try {
                            billDto.payDate = sdf.parse(value[3]);
                            billDto.inputDate = billDto.payDate;
                        } catch (ParseException e) {
                            sendStringForLog("ошибка в дате чека. => "+ value[3]+ " \n "+ e.toString(), h);
                            continue;
                        }

                        if(value.length == 10) {
                            billDto.uuid = value[9];
                            if(billDto.uuid != null) {
                                billDto.uuid = billDto.uuid.trim();
                            }
                        }

                        if(billDto.kind.equals("")) {
                            billDto.kind = "Прочие";
                            billDto.description += ". Пустой вид затрат.";
                        } else {
                            billDto.kind = billDto.kind.substring(0,1).toUpperCase()
                                    + billDto.kind.substring(1).toLowerCase();
                        }

                        if(billDto.uuid == null || billDto.uuid.equals("")) {
                            billDto.uuid = "OLD_UUID_"+ value[7];
                        }

                        calendar.setTime(billDto.payDate);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        if(year != currentYear || month != currentMonth ) {
                            createAndSendMail(currentYear, currentMonth, checksForSend);

                            checksForSend.clear();
                            currentYear = year;
                            currentMonth = month;
                        }

                        BillDto billDtoFromDb = db.getBillDtoByUuid(billDto.uuid);
                        if(billDtoFromDb == null) {
                            checksForSend.add(billDto);
                        }
                    }
                    db.close();

                    createAndSendMail(currentYear, currentMonth, checksForSend);
                    sendStringForLog("старые чеки отправлены успешно.", h);

                } catch (Exception ex) {
                    StringWriter sWr = new StringWriter();
                    PrintWriter pw = new PrintWriter(sWr);
                    ex.printStackTrace(pw);
                    sendStringForLog(sBillFieldVlues, h);
                    sendStringForLog(sWr.toString(), h);
                }
            }

            private void createAndSendMail(int currentYear, int currentMonth, List<BillDto> billsForMonth) {
                if(billsForMonth.size() > 0) {
                    Mail m = new Mail(mailProperties.getLoginName(),
                            mailProperties.getLoginPassword());
                    m.setPort("" + mailProperties.getSMTPport());
                    m.setHost(mailProperties.getSMTPServerAddress());

                    m.setTo(new String[]{mailProperties.getRecipient()});
                    m.setFrom(mailProperties.getSender());
                    m.setSubject(MailPartsCreator.getMessageSubject());

                    BillDto billDtos[] = billsForMonth.toArray(new BillDto[billsForMonth.size()]);

                    m.setBody(MailPartsCreator.getMessageBody(SendBillsActivity.this, billDtos).toString());

                    try {
                        m.send();
                        sendStringForLog("Чеки за " + currentMonth + " месяц " + currentYear + " отправлены успешно.", h);
                    } catch (Exception e) {
                        sendStringForLog("Ошибка. Чеки не отправлены.", h);
                        sendStringForLog(e.toString(), h);
                    }
                }
            }
        };
        new Thread(rMailSender).start();
    }
    */

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
