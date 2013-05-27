package ru.kirill.checksfirstpage;

import java.text.DecimalFormat;

import ru.kirill.checksfirstpage.db.Db;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by oleg on 25.05.13.
 */
public class DbContentActivity extends Activity {
    private Db db;
    SimpleCursorAdapter scAdapter;
    Cursor cursor;
    private ListView lvData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_db_content);

        db = new Db(this);
        db.open();

        // получаем курсор
        cursor = db.getAllData();
        startManagingCursor(cursor);

        // формируем столбцы сопоставления
        String[] from = new String[]{Db.COLUMN_ID, Db.COLUMN_CASH, Db.COLUMN_PAY_DATE, Db.COLUMN_KIND, Db.COLUMN_DESCRIPTION};
        int[] to = new int[]{R.id.tvId, R.id.tvCash, R.id.tvPayDate, R.id.tvKind, R.id.tvDescription};

        // создааем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(this, R.layout.act_cash_list_item, cursor, from, to) {
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


            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                int idxId = cursor.getColumnIndex(Db.COLUMN_ID);
                int idxCash = cursor.getColumnIndex(Db.COLUMN_CASH);
                int idxKind = cursor.getColumnIndex(Db.COLUMN_KIND);
                int idxPayDate = cursor.getColumnIndex(Db.COLUMN_PAY_DATE);
                int idxDescription = cursor.getColumnIndex(Db.COLUMN_DESCRIPTION);

                TextView vId = (TextView) view.findViewById(R.id.tvId);
                long id = cursor.getLong(idxId);
                vId.setText(""+id);


                TextView vCash = (TextView) view.findViewById(R.id.tvCash);
                vCash.setText(df.format(cursor.getFloat(idxCash)));

                TextView vKind = (TextView) view.findViewById(R.id.tvKind);
                vKind.setText(cursor.getString(idxKind));
				
				TextView vDatePay = (TextView) view.findViewById(R.id.tvPayDate);
			    vDatePay.setText(cursor.getString(idxPayDate));
                
			    TextView vDescription = (TextView) view.findViewById(R.id.tvDescription);
			    vDescription.setText(cursor.getString(idxDescription));
            }
        };
        lvData = (ListView) findViewById(R.id.lvData);
        lvData.setAdapter(scAdapter);
    }
}
