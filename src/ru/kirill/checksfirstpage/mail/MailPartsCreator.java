package ru.kirill.checksfirstpage.mail;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;

import ru.kirill.checksfirstpage.db.Db;
import ru.kirill.checksfirstpage.dto.BillDto;
import ru.kirill.checksfirstpage.util.UtilAndroid;

/**
 * Created by oleg on 01.06.13.
 */
public class MailPartsCreator {
    public static String getMessageSubject()
    {
        return "BillsSystem;version=1.0;check.version=1.2";
    }
    public static StringBuilder getMessageBody(Context ctx) {
        StringBuilder sb = new StringBuilder();
        Db db = new Db(ctx);
        db.open();

        // получаем курсор

        BillDto bill = new BillDto();
        Cursor cursor = db.getAllData();

        sb.append("phone.IMEI=").append(UtilAndroid.getIMEI()).append("\n")
                .append("fields=date;cash;kind;description;uuid;inputdate\n")
                .append("statistic::bills.count=")
                .append(""+cursor.getCount()).append("\n");
        int i = 0;
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.yyyy");
        while (cursor.moveToNext()) {
            db.fillBillFields(cursor, bill);
            sb.append("bill.npp=").append(Integer.toString(i)).append("::")
                    .append("bill.fields=")
                    .append(dateFmt.format(bill.payDate)).append(";")
                    .append(bill.cash).append(";")
                    .append(bill.kind).append(";")
                    .append(bill.description).append(";")
                    .append(bill.uuid).append(";")
                    .append("bill.inputDate").append("\n");
            i++;
        }
        cursor.close();
        db.close();

        return sb;
    }
}
