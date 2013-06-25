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
import ru.kirill.checksfirstpage.util.Helper;

/**
 * Created by K on 11.06.13.
 */
public class BillsByYearMonthsActivity extends BillsByXActivity {

    static public int year;
    private float mMaxSum;



    @Override
    protected String getHeaderText() {
        return "Список затрат по месяцам в " + year + " году";
    }

    @Override
    protected int getRecordItemLayout() {
        return R.layout.bills_by_kind_item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cursor.moveToFirst();
        float tmpMaxSum = 0;
        do {
            tmpMaxSum = Math.max(tmpMaxSum, cursor.getFloat(mIdxCash));
        } while (cursor.moveToNext());

        mMaxSum = tmpMaxSum;
    }

    @Override
    protected Cursor getCursor() {
        return db.getDataByYearMonths(year);
    }

    @Override
    protected void onBindView(View view, Context context, Cursor cursor) {

        ProgressBar pBar = (ProgressBar) view.findViewById(R.id.progressBar);
        pBar.setMax((int) mMaxSum);
        pBar.setProgress((int) cursor.getFloat(mIdxCash));
    }

    @Override
    protected String getKindText(Cursor cursor) {
        return Helper.getMonthName(cursor.getInt(mIdxCaption));
    }

    @Override
    protected Intent onDataClick(int id) {
        Intent intent = new Intent(BillsByYearMonthsActivity.this, BillsByYearMonthKindsActivity.class);
        BillsByYearMonthKindsActivity.year = year;
        BillsByYearMonthKindsActivity.month = cursor.getInt(mIdxCaption);
        return intent;

    }

    @Override
    protected int getCurrentYear() {
        return year;
    }
}
