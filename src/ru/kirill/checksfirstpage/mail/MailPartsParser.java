package ru.kirill.checksfirstpage.mail;

import ru.kirill.checksfirstpage.dto.BillDto;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by oleg on 07.06.13.
 */
public class MailPartsParser {
    List<BillDto> bills = new LinkedList<BillDto>();
    int lineNumber = 0;
    private String phoneImei;
    private String[] fields;
    private String statistic;

    public void parseBodyLine(String line) {
        lineNumber++;
        switch (lineNumber) {
            case 1:
                setPhoneImei(line);
                break;
            case 2:
                setFields(line);
                break;
            case 3:
                setStatistic(line);
                break;
            default:
                addBill(line);
        }
    }

    private SimpleDateFormat dfDate = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat dfInputDateJME =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
    //                            Thu May 14 07:56:34 GMT+03:00 2009

    private static SimpleDateFormat dfDateTimeOffet =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
    //                            Thu May 14 07:56:34 +0300 2009

    private SimpleDateFormat dfInputDateAndroid =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
    //                            2013-06-07 10:04:02.0

    private void addBill(String line) {
// fields=date;cash;kind;description;uuid;inputdate
// bill.npp=1::bill.fields=30.04.2009;300.0;Телефон;Дима;37308b70-403b-11de-aeff-502758a69fee;Thu May 14 07:56:34 GMT+03:00 2009
// [0       ]  [1                                                                                                              ]
//             [0        ] [1                                                                                                  ]
//                          date      cash  kind    description uuid                          inputDate
//                          0         1     2       3           4                             5
        try {
            String [] parts = line.split("::")[1].split("=")[1].split(";");
            // inputDate - вариант JME "Wed Jun 13 20:18:33 GMT+00:00 2012"
            //             вариант Android "2013-06-07 10:04:02.0"
            BillDto billDto = new BillDto();
            billDto.expImp = 2; // 2 - загружен из письма
            billDto.payDate = dfDate.parse(parts[0]);
            billDto.cash = parts[1];
            billDto.kind = parts[2];
            billDto.description = parts[3];
            billDto.uuid = parts[4];

            SimpleDateFormat dfInputDate = dfInputDateJME;
            // inputDate
            char firstChar = parts[5].charAt(0);
            if('0' <= firstChar && firstChar <= '9') {
                dfInputDate = dfInputDateAndroid;
            } else if(parts[5].indexOf(" +") > -1) {
                // 07:56:34 +0300 2009
                dfInputDate = dfDateTimeOffet;
            }
            billDto.inputDate = dfInputDate.parse(parts[5]);

            bills.add(billDto);
        } catch (Exception ex) {
            throw new RuntimeException(line, ex);
        }
    }


    public List<BillDto> getBills() {
        return bills;
    }

    public void setPhoneImei(String phoneImei) {
        this.phoneImei = phoneImei;
    }

    public String getPhoneImei() {
        return phoneImei;
    }

    public void setFields(String _fields) {
// fields=date;cash;kind;description;uuid;inputdate
        this.fields = _fields.split("=")[1].split(";");
    }

    public String[] getFields() {
        return fields;
    }


    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public String getStatistic() {
        return statistic;
    }
}
