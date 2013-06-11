package ru.kirill.checksfirstpage.mail;

import android.content.Context;
import android.database.Cursor;
import ru.kirill.checksfirstpage.db.Db;
import ru.kirill.checksfirstpage.dto.BillDto;
import ru.kirill.checksfirstpage.util.UtilAndroid;

import java.text.SimpleDateFormat;
import java.util.Enumeration;

/**
 * Created by oleg on 01.06.13.
 */
public class MailPartsCreator {
    public static String getMessageSubject()
    {
        return "BillsSystem;version=1.0;check.version=1.2";
    }
    public static StringBuilder getMessageBody(Context ctx, Db db, long[] ids) {
        StringBuilder sb = new StringBuilder();

        // получаем курсор

        sb.append("phone.IMEI=").append(UtilAndroid.getIMEI(ctx)).append("\n")
                .append("fields=date;cash;kind;description;uuid;inputdate\n")
                .append("statistic::bills.count=")
                .append(""+ids.length).append("\n");
        int i = 0;

        for(long id: ids) {
            BillDto bill = db.get(id);
            sb.append("bill.npp=").append(Integer.toString(i)).append("::");
            sb.append(serializeBillDto(bill));
            sb.append("\n");
            i++;
        }

        return sb;
    }

    private static SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.yyyy");

    public static String serializeBillDto(BillDto bill) {
        return   "bill.fields="
                +dateFmt.format(bill.payDate)+";"
                +bill.cash+";"
                +bill.kind+";"
                +bill.description+";"
                +bill.uuid+";"
                +bill.inputDate;
    }


    public static long[] getNewBillsIds(Context ctx) {
        return getBillsIds(ctx, 0); // новые чеки
    }

    public static long[] getNotImportedAndEditedBillsIds(Context ctx) {
        return getBillsIds(ctx, 1); // Чеки введённые на этом устройстве
    }

    public static long[] getBillsIds(Context ctx, int impExp) {
        Db db = new Db(ctx);
        db.open();
        Cursor cursor = db.getData(impExp);
        int idIdx = cursor.getColumnIndex("_id");
        long[] result = new long[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            result[i++] = cursor.getLong(idIdx);
        }
        cursor.close();
        db.close();
        return result;
    }
}
