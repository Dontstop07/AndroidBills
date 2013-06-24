package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import ru.kirill.checksfirstpage.db.Db;

/**
 * Created by K on 12.06.13.
 */
public abstract class BillsByXActivity extends Activity implements View.OnClickListener {
    protected SimpleCursorAdapter scAdapter;
    protected Cursor cursor;
    protected Db db;
    protected ListView lvData;
    protected int mIdxCash;
    protected int mIdxCaption;
    Button btnFilter;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bills_by_year);
        TextView tv = (TextView) findViewById(R.id.tvHeader);
        tv.setText(getHeaderText());

        btnFilter = (Button) findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(this);

        db = new Db(this);
        db.open();

        // получаем курсор
        cursor = getCursor();
        startManagingCursor(cursor);

        mIdxCash = cursor.getColumnIndex(Db.COLUMN_CASH);
        mIdxCaption = 1;


        // формируем столбцы сопоставления
        //        String[] from = new String[]{Db.COLUMN_ID, Db.COLUMN_CASH, Db.COLUMN_PAY_DATE, Db.COLUMN_KIND, Db.COLUMN_DESCRIPTION};
        //        int[] to = new int[]{R.id.tvId, R.id.tvCash, R.id.tvPayDate, R.id.tvKind, R.id.tvDescription};

        // создааем адаптер и настраиваем список
        lvData = (ListView) findViewById(R.id.lvData);
//      scAdapter = new SimpleCursorAdapter(this, R.layout.act_cash_list_item, cursor, from, to);

        scAdapter = new SimpleCursorAdapter(this, getRecordItemLayout(), cursor, new String[0], new int[0]) {
            DecimalFormat df = new DecimalFormat("#.00");
            {
                //                df.setMaximumFractionDigits(2);
                //                df.setMinimumFractionDigits(2);
            }
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(getRecordItemLayout(), parent, false);
                return v;
                //                return super.newView(context, cursor, parent);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView vCash = (TextView) view.findViewById(R.id.tvItemSum);
                vCash.setText(df.format(cursor.getFloat(mIdxCash)));

                TextView vKind = (TextView) view.findViewById(R.id.tvItemCaption);
                vKind.setText(cursor.getString(mIdxCaption));

             onBindView(view, context, cursor);
            }
        };
        lvData = (ListView) findViewById(R.id.lvData);
        lvData.setAdapter(scAdapter);
        //registerForContextMenu(lvData);

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(BillsByXActivity.this, "" + id, Toast.LENGTH_SHORT).show();
                Intent intent = onDataClick((int) id);
                startActivity(intent);

            }
        });
    }

    protected abstract int getRecordItemLayout();


    protected abstract Cursor getCursor();

    protected void onBindView(View view, Context context, Cursor cursor) {
    }

    abstract protected Intent onDataClick(int id);

    protected abstract String getHeaderText();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFilter: {
                FilterKindsActivity.year = getCurrentYear();
                Intent intent = new Intent(this, FilterKindsActivity.class);
                startActivityForResult(intent, 1);
                break; }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Cursor oldCursor = cursor;
            stopManagingCursor(oldCursor);
            cursor = getCursor();
            startManagingCursor(cursor);
            scAdapter.changeCursor(cursor);
            oldCursor.close();
        } else {
            Toast.makeText(this, "Wrong result", Toast.LENGTH_SHORT).show();
        }
    }

    protected int getCurrentYear() {
        return -1;
    }

}

