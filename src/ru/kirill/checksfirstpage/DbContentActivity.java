package ru.kirill.checksfirstpage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Intent;
import ru.kirill.checksfirstpage.db.Db;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import ru.kirill.checksfirstpage.dto.BillDto;

/**
 * Created by oleg on 25.05.13.
 */
public class DbContentActivity extends Activity {
    private Db db;
    SimpleCursorAdapter scAdapter;
    Cursor cursor;
    private ListView lvData;
    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_db_content);

        db = new Db(this);
        db.open();

        // получаем курсор
        cursor = db.getAllData();
        startManagingCursor(cursor);

        // формируем столбцы сопоставлени€
        //        String[] from = new String[]{Db.COLUMN_ID, Db.COLUMN_CASH, Db.COLUMN_PAY_DATE, Db.COLUMN_KIND, Db.COLUMN_DESCRIPTION};
        //        int[] to = new int[]{R.id.tvId, R.id.tvCash, R.id.tvPayDate, R.id.tvKind, R.id.tvDescription};

        // создааем адаптер и настраиваем список
        lvData = (ListView) findViewById(R.id.lvData);
//      scAdapter = new SimpleCursorAdapter(this, R.layout.act_cash_list_item, cursor, from, to);

        scAdapter = new SimpleCursorAdapter(this, R.layout.act_cash_list_item, cursor, new String[0], new int[0]) {
            DecimalFormat df = new DecimalFormat("#.00");
            {
                //                df.setMaximumFractionDigits(2);
                //                df.setMinimumFractionDigits(2);
            }
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(R.layout.act_cash_list_item, parent, false);
                return v;
                //                return super.newView(context, cursor, parent);
            }

            int currentYear;
            int currentMonth;
            String dateFormat[] = {"ccc dd", "ccc dd MMM", "ccc dd MMM yyyy"};
            SimpleDateFormat dateFormatter[];
            int weekEndColor = Color.parseColor("#cc2030");

            {
                Calendar calendarCurrentDate = new GregorianCalendar();
                currentYear = calendarCurrentDate.get(Calendar.YEAR);
                currentMonth = calendarCurrentDate.get(Calendar.MONTH);
                dateFormatter = new SimpleDateFormat[dateFormat.length];
                int i = 0;
                for(String format: dateFormat) {
                    dateFormatter[i++] = new SimpleDateFormat(format);
                }
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                int idxCash = cursor.getColumnIndex(Db.COLUMN_CASH);
                int idxKind = cursor.getColumnIndex(Db.COLUMN_KIND);
                int idxPayDate = cursor.getColumnIndex(Db.COLUMN_PAY_DATE);
                int idxDescription = cursor.getColumnIndex(Db.COLUMN_DESCRIPTION);

                TextView vCash = (TextView) view.findViewById(R.id.tvCash);
                vCash.setText(df.format(cursor.getFloat(idxCash)));

                TextView vKind = (TextView) view.findViewById(R.id.tvKind);
                vKind.setText(cursor.getString(idxKind));

                String sDatePay = cursor.getString(idxPayDate);
                //  2013-05-04
                //  yyyy mm dd
                String[] sParts = sDatePay.split("(-| )");
                int year = Integer.parseInt(sParts[0]);
                int month = Integer.parseInt(sParts[1]);
                int day = Integer.parseInt(sParts[2]);
                Calendar calendar = new GregorianCalendar();
                calendar.set(year, month, day);
                int formatterIndex = 2;
                if(year == currentYear) {
                    formatterIndex = 1;
                    if(month == currentMonth) {
                        formatterIndex = 0;
                    }
                }

                //			    vDatePay.setText(cursor.getString(idxPayDate));
                String sFormattedDate = dateFormatter[formatterIndex].format(calendar.getTime());
                String sWeekDay = "";
                int posSpace = sFormattedDate.indexOf(" ");
                if(posSpace > -1) {
                    sWeekDay = sFormattedDate.substring(0, posSpace);
                    sFormattedDate = sFormattedDate.substring(posSpace).trim();
                }

                TextView vPayDate = (TextView) view.findViewById(R.id.tvPayDate);
                vPayDate.setText(sFormattedDate);

                TextView vPayDateWeekDay = (TextView) view.findViewById(R.id.tvPayDateWeekDay);
                vPayDateWeekDay.setText(sWeekDay);

                boolean vSb = calendar.get(Calendar.DAY_OF_WEEK) == 7; // —уббота
                boolean vVs = calendar.get(Calendar.DAY_OF_WEEK) == 1; // ¬оскресение
                if(vSb || vVs) {
                    vPayDateWeekDay.setTextColor(weekEndColor);
                } else {
                    vPayDateWeekDay.setTextColor(vPayDate.getCurrentTextColor());
                }


                TextView vDescription = (TextView) view.findViewById(R.id.tvDescription);
                vDescription.setText(cursor.getString(idxDescription));
            }
        };
        lvData = (ListView) findViewById(R.id.lvData);
        lvData.setAdapter(scAdapter);
        registerForContextMenu(lvData);
    }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
        menu.add(0, CM_EDIT_ID, 0, R.string.edit_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удал€ем соответствующую запись в Ѕƒ
            db.delRec(acmi.id);
            // обновл€ем курсор
            cursor.requery();
            return true;
        } else if (item.getItemId() == CM_EDIT_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            long id = acmi.id;
            BillDto billDto = db.get(id);
            BillActivity.billDto = billDto;
            BillActivity.editMode = 1;
            Intent intent = new Intent(this, BillActivity.class);
            startActivity(intent);
            // извлекаем id записи и удал€ем соответствующую запись в Ѕƒ
            Toast.makeText(this, "редактирование", Toast.LENGTH_SHORT).show();

            // обновл€ем курсор
            // веро€тно обновление курсора нужно поместить в обработчик событи€ которое происходит
            // когда текущее активити станет оп€ть станет активным
            cursor.requery();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
