package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import ru.kirill.checksfirstpage.db.Db;

/**
 * Created by K on 11.06.13.
 */
public class BillsByYearMonthsActivity extends Activity {

    private Db db;
    SimpleCursorAdapter scAdapter;
    Cursor cursor;
    private ListView lvData;
    static public int year;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bills_by_year);
        TextView tv = (TextView) findViewById(R.id.tvHeader);
        tv.setText("—писок затрат по мес€цам в " + year + " году");

        db = new Db(this);
        db.open();

        // получаем курсор
        cursor = db.getDataByYearMonths(year);
        startManagingCursor(cursor);

        final int idxCash = cursor.getColumnIndex(Db.COLUMN_CASH);
        final int idxCaption = 1; //cursor.getColumnIndex(Db.COLUMN_CAPTION);

        cursor.moveToFirst();
        float tmpMaxSum = 0;
        do {
            tmpMaxSum = Math.max(tmpMaxSum, cursor.getFloat(idxCash));
        } while (cursor.moveToNext());

        final float maxSum = tmpMaxSum;

        // формируем столбцы сопоставлени€
        //        String[] from = new String[]{Db.COLUMN_ID, Db.COLUMN_CASH, Db.COLUMN_PAY_DATE, Db.COLUMN_KIND, Db.COLUMN_DESCRIPTION};
        //        int[] to = new int[]{R.id.tvId, R.id.tvCash, R.id.tvPayDate, R.id.tvKind, R.id.tvDescription};

        // создааем адаптер и настраиваем список
        lvData = (ListView) findViewById(R.id.lvData);
//      scAdapter = new SimpleCursorAdapter(this, R.layout.act_cash_list_item, cursor, from, to);

        scAdapter = new SimpleCursorAdapter(this, R.layout.bills_by_kind_item, cursor, new String[0], new int[0]) {
            DecimalFormat df = new DecimalFormat("#.00");
            {
                //                df.setMaximumFractionDigits(2);
                //                df.setMinimumFractionDigits(2);
            }
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(R.layout.bills_by_kind_item, parent, false);
                return v;
                //                return super.newView(context, cursor, parent);
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy");


            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView vCash = (TextView) view.findViewById(R.id.tvItemSum);
                vCash.setText(df.format(cursor.getFloat(idxCash)));

                TextView vKind = (TextView) view.findViewById(R.id.tvItemCaption);
                vKind.setText(cursor.getString(idxCaption));

                ProgressBar pBar = (ProgressBar) view.findViewById(R.id.progressBar);
                pBar.setMax((int) maxSum);
                pBar.setProgress((int) cursor.getFloat(idxCash));
            }
        };
        lvData = (ListView) findViewById(R.id.lvData);
        lvData.setAdapter(scAdapter);
        //registerForContextMenu(lvData);

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(BillsByYearMonthsActivity.this, ""+id, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BillsByYearMonthsActivity.this, BillsByYearMonthKindsActivity.class);
                BillsByYearMonthKindsActivity.year = year;
                BillsByYearMonthKindsActivity.month = (int) cursor.getInt(idxCaption);
                startActivity(intent);

            }
        });
    }

}


