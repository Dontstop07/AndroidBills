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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.kirill.checksfirstpage.db.Db;

/**
 * Created by K on 11.06.13.
 */
public class BillsByYearMonthKindsActivity extends BillsByXActivity {

    static public int year;
    static public int month;
    private float mMaxSum;

    @Override
    protected String getHeaderText() {
        return "Список видов затрат. год: " + year + " месяц: " + month;
    }

    @Override
    protected int getRecordItemLayout() {
        return R.layout.bills_by_kind_item;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cursor.moveToFirst();
        float tmpMaxSum = 0;
        do {
            tmpMaxSum = Math.max(tmpMaxSum, cursor.getFloat(mIdxCash));
        } while (cursor.moveToNext());

        mMaxSum = tmpMaxSum;
    }

    protected Cursor getCursor() {
        return db.getDataByYearMonthKinds(year, month);
    }

    @Override
    protected void onBindView(View view, Context context, Cursor cursor) {
        ProgressBar pBar = (ProgressBar) view.findViewById(R.id.progressBar);
        pBar.setMax((int) mMaxSum);
        pBar.setProgress((int) cursor.getFloat(mIdxCash));
    }

    @Override
    protected Intent onDataClick(int id) {
        Intent intent = new Intent(BillsByYearMonthKindsActivity.this, DbContentActivity.class);
        DbContentActivity.useSelected = true;
        DbContentActivity.selectedYear = year;
        DbContentActivity.selectedMonth = month;
        DbContentActivity.selectedKind = cursor.getString(mIdxCaption);
        return intent;

    }

    @Override
    protected int getCurrentYear() {
        return year;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFilter: {
                BillActivity.editMode = 0;
                Intent intent = new Intent(this, BillActivity.class);
                startActivity(intent);
                BillActivity.billDto.cash = "";
                BillActivity.billDto.payDate = new Date();
                BillActivity.billDto.kind = "";
                BillActivity.billDto.description = "";
                startActivityForResult(intent, 1);
                break; }
        }

    }

}


